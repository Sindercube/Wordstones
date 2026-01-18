package org.sindercube.wordstones.content.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.sindercube.wordstones.registry.WordstonesEntityTypes;
import org.sindercube.wordstones.registry.WordstonesItems;
import org.sindercube.wordstones.registry.WordstonesTags;

public class EnchantedSquidEntity extends SquidEntity {

	public EnchantedSquidEntity(EntityType<? extends SquidEntity> entityType, World world) {
		super(entityType, world);
	}

	public static ActionResult tryTransformSquid(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
		if (!(entity instanceof SquidEntity original)) return ActionResult.PASS;
		if (!entity.getType().isIn(WordstonesTags.ENCHANTABLE_SQUID)) return ActionResult.PASS;
		if (!player.getStackInHand(hand).isOf(Items.DRAGON_BREATH)) return ActionResult.PASS;

		transformSquid(world, original);
		if (world.isClient) return ActionResult.SUCCESS;

		if (!player.getAbilities().creativeMode) {
			player.getStackInHand(hand).decrement(1);
			player.giveItemStack(Items.GLASS_BOTTLE.getDefaultStack());
		}

		return ActionResult.SUCCESS;
	}

	public static void transformSquid(World world, SquidEntity original) {
		EnchantedSquidEntity entity = WordstonesEntityTypes.ENCHANTED_SQUID.create(world);
		if (entity == null) return;

		entity.refreshPositionAndAngles(
			original.getX(), original.getY(), original.getZ(),
			original.getYaw(), original.getPitch()
		);
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(entity.getInkParticle(), entity.getX(), entity.getY(), entity.getZ(), 32, entity.random.nextFloat(), entity.random.nextFloat(), entity.random.nextFloat(), 0.1F);
		}

		world.spawnEntity(entity);
		original.discard();
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new EnchantedSwimGoal(this));
		this.goalSelector.add(1, new EnchantedEscapeAttackerGoal(this));
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		boolean result = super.damage(source, amount);
		if (result) this.getWorld().getNonSpectatingEntities(Entity.class, this.getHitbox().expand(3)).forEach(entity -> {
			if (entity instanceof ItemEntity item) {
				if (item.getStack().isOf(WordstonesItems.EMPTY_QUILL)) item.setStack(WordstonesItems.ENCHANTED_QUILL.getDefaultStack());
			}
			if (entity instanceof PlayerEntity player) {
				if (player.isCreative()) return;
				for (Hand hand : Hand.values()) {
					if (player.getStackInHand(hand).isOf(WordstonesItems.EMPTY_QUILL)) {
						player.setStackInHand(hand, WordstonesItems.ENCHANTED_QUILL.getDefaultStack());
					}
				}
			}
		});
		return result;
	}


	@Override
	public boolean isTouchingWater() {
		return true;
	}

	@Override
	public boolean isInsideWaterOrBubbleColumn() {
		return true;
	}

	@Override
	public boolean isSubmergedInWater() {
		return true;
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected ParticleEffect getInkParticle() {
		return ParticleTypes.SQUID_INK;
	}

	@Override
	protected void tickWaterBreathingAir(int air) {}

	public static class EnchantedSwimGoal extends Goal {

		private final SquidEntity entity;

		public EnchantedSwimGoal(SquidEntity entity) {
			this.entity = entity;
		}

		@Override
		public boolean canStart() {
			return true;
		}

		@Override
		public void tick() {
			if (this.entity.getRandom().nextInt(toGoalTicks(50)) > 0) return;

			float power = this.entity.getRandom().nextFloat() * ((float)Math.PI * 2F);
			float x = MathHelper.cos(power) * 0.2F;
			float y = -0.1F + this.entity.getRandom().nextFloat() * 0.2F;
			float z = MathHelper.sin(power) * 0.2F;
			this.entity.setSwimmingVector(x, y, z);
		}

	}

	public static class EnchantedEscapeAttackerGoal extends Goal {

		private final SquidEntity entity;
		private int timer;

		public EnchantedEscapeAttackerGoal(SquidEntity entity) {
			super();
			this.entity = entity;
		}

		@Override
		public boolean canStart() {
			LivingEntity attacker = this.entity.getAttacker();
			if (attacker == null) return false;

			return this.entity.squaredDistanceTo(attacker) < 64;
		}

		@Override
		public void start() {
			this.timer = 0;
		}

		@Override
		public boolean shouldRunEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			++this.timer;
			LivingEntity attacker = this.entity.getAttacker();
			if (attacker == null) return;

			Vec3d newPosition = this.entity.getPos().subtract(attacker.getPos());
			BlockState state = this.entity.getWorld().getBlockState(
				BlockPos.ofFloored(this.entity.getX() + newPosition.x, this.entity.getY() + newPosition.y, this.entity.getZ() + newPosition.z)
			);
			if (!state.isAir()) return;

			double length = newPosition.length();
			if (length > 0) {
				newPosition.normalize();
				double distance = 3;
				if (length > 5) distance -= (length - 5) / 5;
				if (distance > 0) newPosition = newPosition.multiply(distance);
			}

			this.entity.setSwimmingVector((float) newPosition.x / 20, (float) newPosition.y / 20, (float) newPosition.z / 20);

			if (this.timer % 10 == 5 && this.entity.isTouchingWater()) {
				this.entity.getWorld().addParticle(ParticleTypes.BUBBLE, this.entity.getX(), this.entity.getY(), this.entity.getZ(), 0.0F, 0.0F, 0.0F);
			}
		}

	}

}
