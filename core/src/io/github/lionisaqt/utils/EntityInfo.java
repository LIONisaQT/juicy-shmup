package io.github.lionisaqt.utils;

/**
 * Data structure used to hold information about an entity.
 * A separate Object to pass into the entity's body's setUserData() method.
 * @author Ryan Shee */
public class EntityInfo {
    public short hp, maxHp, dmg, speed;
    public float impact; // How much screen shake this entity produces
    public boolean friendly, isPlayer;
}
