package finalproject.financetracker.utils;

import java.util.function.Function;

@FunctionalInterface
public interface IFunctionalInterface<T,R, E extends Exception> {

    R apply(T t, E e) throws E;
}
