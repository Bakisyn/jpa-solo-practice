package dev.milan.jpasolopractice.shared;

import com.github.fge.jsonpatch.JsonPatch;
import dev.milan.jpasolopractice.customException.ApiRequestException;

public interface Patcher <T>{
     T patch(JsonPatch patch, T t) throws ApiRequestException;
}
