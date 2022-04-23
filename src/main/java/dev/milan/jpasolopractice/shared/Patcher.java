package dev.milan.jpasolopractice.shared;

import com.github.fge.jsonpatch.JsonPatch;

public interface Patcher <T>{
     T patch(JsonPatch patch, T t);
}
