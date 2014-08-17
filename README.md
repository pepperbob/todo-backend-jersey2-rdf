todo-backend-jersey2-rdf
========================

This is an implementation of the http://todo-backend.thepete.net/ specs which uses Jersey2 for handling the REST-part
and Sesame's In-Memory Repository as a RDF-data-storage.

I couldn't find any good RDF-vocabulary to cover "Todo-Lists" so I just created my own properties. If I missed something:
comments are welcome.

As Jersey does not handle HTTP PATCH I implemented a very naive solution which works for this specific use case - 
this got mainly inspired by http://kingsfleet.blogspot.de/2014/02/transparent-patch-support-in-jax-rs-20.html
