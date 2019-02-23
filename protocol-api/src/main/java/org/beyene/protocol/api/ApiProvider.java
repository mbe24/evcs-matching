package org.beyene.protocol.api;

public interface ApiProvider<A, T extends  ApiConfiguration<? extends A, T>> {

     A newApi(T configuration);
}
