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

Getting Started
---------------

> This is still under construction, so these instructions might not be
> right on target. Feedback welcome!

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
    MutableKeyStorage keyStorage = KeyStorageLocator.getMutableInstance("LOCAL", props);

    Key key;    // any SecretKey or PrivateKey would work here, so let's make
                // an Elliptic Curve (EC) key...
                
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
    kpg.initialize(256);
    
    KeyPair keyPair = kpg.generateKeyPair();
    key = keyPair.getPrivate();
    
    keyStorage.store("some-key-id", key);
   
    assert key.equals(keyStorage.retrieve("some-key-id"));
  }
}

```

After running the demo, you should see a file named `some-key-id.pem` in the
directory you used for the `storageDirectory` property. If you looking in that 
file, you'll see something like the following.

```text
-----BEGIN EC PRIVATE KEY-----
Proc-Type: 4,ENCRYPTED
DEK-Info: PBEWithHmacSHA512AndAES_256/CBC/PKCS5Padding,4096,d/os8ETcWT5fkUGWsmFWYNVWE3s=,t1a33uI6pqneLnkWjClpyw==

SndZ+8puWj+WVP/5jZjsn1WdwVxmehgx8ZpetquFWhbpk4YJTMSh0KPm6rUmIhql
mO6FT+W/CQDBVZOK3RBshws1jSh0Ztq9Z5dpurWEGSQ=
-----END EC PRIVATE KEY-----

```

We'll get into more of the details below, but what you're looking at is a
fairly common plain-text encoding for a private key; a couple of headers
followed by a base-64 encoded body that is the encrypted private key we stored. 

The `DEK-Info` header provides the configuration details needed to initialize a 
Java [Cipher](https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html)
instance; it basically indicates that what's in the body of this file was 
encrypted using password-based encryption (PBE) using a SHA-512 HMAC and
AES-256. The rest of the details in that header are an iteration count, salt,
and initialization vector (IV) that are needed in order to successfully 
decrypt the the key (assuming that S2KS has the correct password).

It's important to note that none of the things in this file are secret. This 
file is _absolutely useless_ to anyone who does not possess the master password 
(specified in the file identified by the `passwordFile` property in the demo).
Of course, keeping a password secret can be really challenging, and putting the
password in an ordinary file on the filesystem isn't really all that secure. 
It's good enough for applications under development that aren't handling real
users or real data, but for production-grade applications you'll want something
better.

There are some secrets management approaches for container-based applications 
that run inside orchestration frameworks such as Kubernetes, Docker Swarm,
or Amazon ECS. These approaches generally arrange to make secrets available 
inside a running container by placing the secrets in a file on the container
filesystem. Usually the file is on a `tmpfs` filesystem and is generally 
removed as soon as the application has retrieved it. Using a  secrets manager, 
you get _better_ protection for your stored keys; certainly much better than 
simply having the password in an ordinary file on the host filesystem.

However, we can provide _much better_ protection for stored keys using a _key 
management service_ (KMS), as shown in the following example.

#### Get a MutableKeyStorage that stores keys using AWS KMS and S3 

This example stores keys in an S3 bucket associated with an Amazon Web Services 
account. For each key that you store using `MutableKeyStorage`, the AWS KMS 
service is used to generate a unique encryption key using a designated 
_customer master key (CMK)_.  

In order to do this in our example, we'll need to have access to a KMS customer
master key and permission to store and retrieve files in an S3 bucket.

> For the purpose of this demo, we'll assume you'll be using an IAM user with 
> an access key and secret to access the master key and the S3 bucket. However,
> when running applications on AWS EC2 machines or in the Elastic Container
> Service (ECS), you should use an IAM role assigned to your EC2 machine 
> instances or assigned as an ECS Service Task Role. This ensures that only your 
> running application can access the master key and S3 bucket, without requiring 
> you to manage IAM access keys and secrets. See the AWS documentation on using 
> IAM roles for these purposes.  

In order to run this example, you'll first need to do the following in your AWS 
account.

1. [Create an IAM user](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_users.html)
   and create an access key for that user.
2. [Create a customer master key (CMK)](https://docs.aws.amazon.com/kms/latest/developerguide/create-keys.html)
   and give the IAM user permission to use the master key. When creating the
   master key, assign the alias name _s2ks-demo_ to it; the example uses that
   alias name to identify the master key.
3. [Create an S3 bucket](https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingBucket.html)
   and give the IAM user permission to store and retrieve objects in the bucket.
   
You'll also need to configure the access key on your workstation the same as you 
would to use the AWS CLI; using [named profiles](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html)
or [environment variables](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-envvars.html).
These methods make the access key and associated secret available to the 
example when it runs.

Once everything is set up, you should be able to run the example, by 
modifying the constants shown here to match what you set up in your AWS 
account. The example as written assumes you created a named profile with
your access key and secret; if you want to set up a environment variables
instead, simply use `EnvironmentVariableCredentialsProvider` instead of the 
`ProfileCredentialsProvider`.

```java
class AwsDemo {

  static final String REGION = "US_EAST_1";
  static final String IAM_PROFILE = "PROFILE NAME HERE";
  static final String KMS_MASTER_KEY_ID = "alias/s2ks-demo";
  static final String S3_BUCKET_NAME = "BUCKET NAME HERE";
  static final String S3_PREFIX = "keys";

  public static void main(String[] args) throws Exception {
    AWSCredentialsProvider credentialsProvider =
        new ProfileCredentialsProvider(IAM_PROFILE);

    Properties properties = new Properties();
    properties.put("credentialsProvider", credentialsProvider);
    properties.setProperty("region", REGION);
    properties.setProperty("kmsMasterKeyId", KMS_MASTER_KEY_ID);
    properties.setProperty("s3BucketName", S3_BUCKET_NAME);
    properties.setProperty("s3Prefix", S3_PREFIX);

    MutableKeyStorage keyStorage =
        KeyStorageLocator.getMutableInstance("AWS", properties);

    Key key;    // any SecretKey or PrivateKey would work here, let's use
                // an RSA key this time...

    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);

    KeyPair keyPair = kpg.generateKeyPair();
    key = keyPair.getPrivate();

    keyStorage.store("some-key-id", key);

    assert key.equals(keyStorage.retrieve("some-key-id"));

  }

}
```

If you run the demo and get an error message of 
_ProviderConfigurationException: found no key storage provider named 'AWS'_,
make sure you included the `s2ks-aws` dependency.

After successfully running the demo, you look in the S3 bucket you should see
a "folder" named `keys`, which contains a "file" named `some-key-id.pem`. The
`keys` folder was specified using the `s3Prefix` configuration property, and
comes in handy when a bucket is used to store other sorts of configuration in
addition to storing keys. If not specified, the default prefix is empty, which
stores your keys at the "root" of the bucket. 

If you open the `some-key-id.pem` file, you'll see that it contains something 
like the following.

```
-----BEGIN AWS SECRET KEY-----
Key-Id: arn:aws:kms:us-east-1:775521606509:key/af4eeb4a-781b-4c3b-92f7-1007b3f15961

AQIDAHjt4DV9ke69uqJ3+hlyseT8FkzlFzi3e3xWVC23OPX88AFGUB21Ujdc3/4d
C7IyxYVuAAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglg
hkgBZQMEAS4wEQQMm15ZAWFs/AMl+biEAgEQgDvd5rxpqjbeKJYmgyWEkNalAsRy
C42LNcZbIvUEr3jUazlADxv64nvFKG+QtU3dj/wImhxX+PdlydffnQ==
-----END AWS SECRET KEY-----
-----BEGIN RSA PRIVATE KEY-----
Proc-Type: 4,ENCRYPTED
DEK-Info: AES/CBC/PKCS5Padding,JElp3PxT8Rkp4vrR1ykaWQ==

PEtpNjk9VuGRUxAbkoP/V4XghUk9mwol2ZiK22BqbAg385xDQdiN9dQeiRZ0YW3K
I5zc0fG1BEK/1kCwz0zkzAcRtoRrEmHIFw0yRiwEjCYie62hQjWnmLddISkmgM05
hbzqXVkn5pi9IvFE+BOWMoeWhJkqaL6El7DJA17BdDo0Jx7lwNhvC24ZnSzQpN1O
...snip...
hA6xtORQS34w0Ye+wF4+QCnNpo16WALF87LnEIRWGpM=
-----END RSA PRIVATE KEY-----
```

In the local storage example, the key file contained just a single encrypted 
key. But when running using the AWS key storage provider, the file contains 
two keys. The first key is a _data encryption key_ that was generated by the
AWS KMS using your master key. The second key is the example RSA private key
that we stored, encrypted using the first key. The headers on this key 
indicate it was encrypted using an AES cipher, and like our previous example
the `DEK-Info` provides additional non-secret details such as the initialization 
vector (IV) needed to initialize the cipher when decrypting the key.

When the KMS generates a data encryption key it provides the plaintext version 
of the key, which S2KS uses to encrypt the key to be stored and then securely
discards from memory. The KMS also returns an  encrypted version of the data 
encryption key, which can only be decrypted via the KMS, using the same master
key. S2KS stores the encrypted version of this key in the file as the 
_AWS SECRET KEY_.

When the stored key is retrieved by calling `KeyStorage.retrieve`, S2KS passes 
the blob from the first key in the file to back to the KMS and asks the KMS to 
decrypt it using your master key. This provides the plaintext data encryption 
key that is then used to decrypt the stored RSA private key.

There are several advantages to this approach over using password-based 
encryption in S2KS.

1. The master key never leaves the AWS-managed _hardware security module_ (HSM)
   in which it is stored. All encryption and decryption operations that use 
   the master key are performed on the HSM. S2KS can use the master key to 
   generate new encryption keys and to decrypt previously issued encryption 
   keys, but it never gets direct access to the master key.
2. The master key is protected using policies enforced by the AWS key management
   service. In production-grade applications that make use of IAM roles assigned
   to EC2 machines or ECS services, this ensures that only your application can
   use your stored keys; because no other entity can access the master key
   needed to decrypt them.
3. AWS KMS transparently handles master key rotation. A data encryption key
   generated by the KMS includes metadata that identifies the master key that
   was used to generate it, and the KMS keeps a history of your master keys in
   the HSM so that older keys remain available to decrypt keys previously stored
   by S2KS. If you want to re-encrypt a key stored by S2KS using a newer 
   generation of the master key, you need only retrieve the key and then call 
   `MutableKeyStore.store` to cause the key to be re-encrypted using the latest
   generation of the master key.
   