# About

**Wordstones** is a Waystones inspired mod for fast travel

# Features

## Wordstone
Waystone inspired teleportation block
- Placing it prompts you to enter a unique 4-letter combo
- Using it prompts you to write any existing 4-letter combo and teleport
- Teleporting drops all of your items on the ground
  - The `keepInventory` gamerule prevents this

## Drop Box
Per Player inventory storage block
- Stores an inventory for each player that interacts with it
- Using it takes out all the items for your inventory
- Using it while sneaking places all of your items in its inventory
- When placed next to a Wordstone, all dropped items go in the Drop Box instead
  - Can be disabled with the `wordstoneDropBoxInteraction` gamerule

## Last Will
Death inventory protecting item
- Using it on a Drop Box binds its location
- After you die, if you are holding the bound Last Will, it sends your items to the Drop Box instead of dropping them
