![Build Status](https://img.shields.io/travis/vanillasource/gerec.svg)
![Published Version](https://img.shields.io/maven-central/v/com.vanillasource.gerec/gerec-parent.svg)

Gerec, the Generic REST HTTP Client
===================================

A small wrapper over HTTP Clients to support building a proper RESTful
(Hypertext-enabled) API client. Specific features:

* Media-type centered
* Following links made easy
* Navigating complex use-cases made easy
* Easy cache control
* Conditional requests
* Very simple and minimal API
* Asynchronous design
* Zero-copy operation possible

### Getting the library

Add this dependency to your Maven build:

```xml
<dependency>
   <groupId>com.vanillasource.gerec</groupId>
   <artifactId>gerec</artifactId>
   <version>2.0.0</version>
</dependency>
```

To include the "API" of Gerec. If you want to instantiate a `ResourceReference` you
need to include `gerec-httpclient` for example to use the Apache HttpClient implementation.
To use Jackson for the media-types, include `gerec-jackson`.

### The basics

The central concept in Gerec is a `ResourceReference`. This is
an object that can be used to manipulate the remote resource it points to.
These references are not instantiated by the user, as often is the case with other
frameworks, but they are created by following links or submitting forms. The only exception is the very
first reference, created from a bookmark to some entry point into a web of resources.

The reference contains the usual methods to manipulate the resource it points to:
* `GET`
* `POST`
* `PUT`
* `DELETE`
* `HEAD`
* `OPTIONS`

The result of any of the HTTP methods will result in a `Response`. A `Response` can contain
a response body, the format of which is described by a `MediaType`. The content of the `Response` might / should also
include additional information for making subsequent requests, such as links (additional
`ResourceReference` objects) or forms.

### Some random examples

Simple `GET` requests are made the following way:

```java
String body = reference.get(MediaTypes.textPlain()).join();
```

Media types can be freely created for any representation, as they should be. So `GET`ting
a complex object looks like this:

```java
Person person = reference.get(Person.MEDIA_TYPE).join();
```

The given media type will be properly communicated to the server, using the normal HTTP
content negotiation mechanisms.

You can also easily make conditional requests. The following example gets a mutable `Person` object, modifies it,
then tries to update the server, but only if it did not change since it was requested. (Also
known as optimistic locking):

```java
Response<Person> personResponse = currentUserReference.getResponse(Person.TYPE).join();
Person person = personResponse.getContent();
person.setName("New Name");
currentUserReference.put(person, personResponse.ifMatch()).join();
```

The `ifMatch()` condition above will use the `ETag` of the response to make the `PUT` conditional
upon the person not having been changed since it was requested.

### Documentation

The Gerec [API Documentation](http://vanillasource.github.io/gerec/apidocs/).

Please visit the [Gerec Wiki](https://github.com/vanillasource/gerec/wiki) for more information.

