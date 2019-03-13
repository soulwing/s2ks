s2ks
====

Simple, secure key storage for Java applications.

Background
----------

For an OAuth service application I've been working on, I needed a simple and
secure way to store the various cryptographic keys needed by my service 
at runtime. I considered simply using the standard Java `KeyStore` but 
I found there were several limitations and assumptions that made it pretty 
impractical for my needs.

I wanted a simple key storage API, which would allow me to store
and retrieve keys (Java type `Key`) using a string identifier 
(e.g. a `kid` from the header of a JWT). I wanted something that could
be backed by a filesystem, cloud storage (e.g. AWS S3), or even a database. 
I wanted to be able to store either private keys for asymmetric cryptography 
(e.g. RSA or EC keys) or secret keys for symmetric cryptography. I wanted to 
be able to easily store newly generated keys at runtime to support things 
like automated key rotation. 

To make it easy to develop applications using the key storage API, I wanted
it to allow me to use a single master password to encrypt keys at rest. 
However, I also wanted the flexibility of using a KMS service (e.g. AWS KMS)
to provide stronger protection for stored keys in production-grade 
deployments. Lastly, I wanted to store keys in a manner that was transparent 
and easily audited for compliance.

After conducting a somewhat broader search for something that really met
my needs, I concluded that it didn't exist, so I created this project,
_Simple Secure Key Storage (S2KS)_ to fill that need. If you have a similar 
need perhaps you will find it useful. The project is open source under the
Apache Software License (ASL). I will consider adding additional licenses,
as well, should the need arise.

Basic Usage
===========

> This is still under construction, so these instructions might not be
> right on target.

#### Add dependencies to your POM
```xml
<dependencies>
  <dependency>
    <groupId>org.soulwing.s2ks</groupId>
    <artifactId>s2ks-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>  
  </dependency>
  <dependency>
    <groupId>org.soulwing.s2ks</groupId>
    <artifactId>s2ks-impl</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>runtime</scope>  
  </dependency>
  <dependency>  <!-- needed only if you want to use the AWS KeyStorage provider -->
    <groupId>org.soulwing.s2ks</groupId>
    <artifactId>s2ks-aws</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>runtime</scope>  
  </dependency>
</dependencies>
``` 

#### Get a MutableKeyStorage that writes to the local filesystem

This example stores keys locally using password-based encryption (PBE). The
master password can be placed in a file, as shown in this example, or can be
specified as a string (using the `password` property).

```java
class Demo {

  public static void main(String[] args) throws Exception {
    Properties props = new Properties();
    props.setProperty("storageDirectory", "/path/to/directory/where/keys/will/be/stored");
    props.setProperty("passwordFile", "/path/to/file/containing/master/password/string");
    MutableKeyStorage keyStorage = KeyStorageLocator.getInstance("LOCAL", props);

    Key key;    // use any SecretKey or PrivateKey here
    
    keyStorage.store("some-key-id", key);
   
    assert key.equals(keyStorage.retrieve("some-key-id"));
  }
}

```

