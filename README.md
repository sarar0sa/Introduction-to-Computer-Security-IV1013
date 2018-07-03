# Introduction-to-Computer-Security-IV1013


## Module 1

### Hill Cipher

A Hill Cipher implementation that consists of three programs:
* **HillKeys** - generates a key matrix K. 
* **HillCipher** - takes as input a plaintext message as a sequence of integers, and an encryption key matrix K. The output is a ciphertext consisting of an integer sequence.
* **HillDecipher** - takes as input a ciphertext message as a sequence of integers, and an encryption key matrix K.  The output consists of a plaintext as a sequence of integers. 

**HillKeys** takes three arguments, and should be executed as follows:

```
$ javac HillKeys.java

$ java HillKeys <radix> <blocksize> <keyfile>
```

**HillCipher** takes five arguments, and should be executed as follows:

```
$ javac HillCipher.java 

$ java HillCipher <radix> <blocksize> <keyfile> <plainfile> <cipherfile>
```

**HillDecipher** takes five arguments, and should be executed as follows:

```
$ javac HillDecipher.java 

$ java HillDecipher <radix> <blocksize> <keyfile> <plainfile> <cipherfile>
```

## Module 3 

### Password Cracker

Dictionary attack performed on a traditional UNIX-style password file.

The file contains salted and hashed passwords. The task was to reveal as many of 
the passwords as possible, by performing a dictionary attack where you generate password guesses, 
hash them, and match the result with the entries in the password file.

**PasswordCrack** should be executed as follows:
```
$ javac PasswordCrack.java 

$ java PasswordCrack <dictionary> <passwd>
```

The first argument <dictionary> is dictionary of words, with one word per line. 
The second argument <passwd> is the password file. The cracked plaintext passwords are printed to the terminal,
one per line.

### Hidden Encryption

Encrypted information will be a “blob” of data that can placed anywhere inside a file.
To the attacker, the encrypted information appears as random data. For someone that knows what to look for,
the data is possible to locate and decrypt. 

The idea are as followed:

* The entire blob is encrypted with a secret key **k**. 
* The first element in the blob is the MD5 hash of the secret key, **H(k)**. 
* Then comes the information to hide, **Data**, followed by **H(k)** again. 
* Last is another hash: the MD5 hash of Data, **H(Data)**.

**H(k)** marks the start and the end of the hidden information and **H(data)** is used to verify that the extracted data is correct.

The project consists of two files: **Hiddec** and **Hidenc**.


**Hiddec** takes a container file as input, extracts a blob from it, and produces the decrypted data from the blob as output. 
Hiddec supports two encryption algorithms: AES-128-ECB and AES-128-CTR.


The program takes the following arguments:
 
```
--key=KEY
Use KEY for encryption key, given as a hexadecimal string.

--ctr=CTR
Use CTR as the initial value for the counter in AES-128-CTR mode. 
Implies that AES-128-CTR mode should be used for encryption (otherwise AES-128-ECB).

 --input=INPUT
Input file. The file INPUT is the container.

 --output=OUTPUT
Output file. OUTPUT is where the decrypted data should be stored.
```

**Hidenc** creates a blob of data and embeds it into a container file. 
Hidenc should supports two encryption algorithms: AES-128-ECB and AES-128-CTR.


The program takes the following arguments:
```
--key=KEY
Use KEY for encryption key, given as a hexadecimal string.

--ctr=CTR
Use CTR as the initial value for the counter in AES-128-CTR mode.
Implies that AES-128-CTR mode should be used for encryption (otherwise AES-128-ECB)

--offset=NUM
Place the blob in the file at an offset of NUM bytes into the container file. 
If no offset is given, Hidenc generates it by random.

--input=INPUT
Input file. The data from file INPUT should be used as the data portion of the blob.

--output=OUTPUT
Output file. OUTPUT is the container where the final result is stored.

--template=TEMPLATE
Use the file TEMPLATE as a template for the container in which the blob should be stored. 
The output file should have exactly the same size the the template. 
In other words, parts of the data in the template should be overwritten with the blob. 
If no template is given, Hidenc generates a container with random data. 
Only one of --template and --size can be specified.

--size=SIZE
The total size of the output file should be SIZE bytes. This implies that the container file 
should be generated automatically. In other words, there is no template. 
Only one of --size and --template can be specified.
```
 
