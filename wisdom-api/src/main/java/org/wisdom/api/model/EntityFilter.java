package org.wisdom.api.model;

/**
 * A class defining filters on entity instances.
 */
public interface EntityFilter<T> {

    /**
     * The filter method let the filter determines if the instance is 'accepted' or not.
     *
     * @param t the entity instance, never {@literal null}
     * @return {@literal true} if the instance is accepted, {@literal false} otherwise.
     */
    public boolean accept(T t);
}
