package com.chuang.urras.toolskit.basic.tree;

public interface IRelation<K, V> {
        K parentID(V t);
        K myID(V t);
}
