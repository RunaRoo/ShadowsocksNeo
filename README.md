This is my first project so don't bully me hard >_<

Author of the original code: https://github.com/zk-123/shadowsocks
Description: 
This is ShadowsocksNeo. Forked from zk-123 shadowsocks java implementation. 
The main goal of the ShadowsocksNeo project is continue development 
of the original protocol and make the usage easy and smooth for everyone 
to fight against internet censorship and protect online anonymity. 

Vulnarabilites:
Stream encryption protocols (camellia-128-cfb, aes-256-cfb and etc.) 
compatible with all shadowsocks clients.
AEAD encryption and 2022-Blake3 
protocols works only with ShadowsocksNeo client.
Too short or too long password may cause encryption errors 

To compile or run: Required java JDK 21+

Usage:
Run .jar file with arguments:
java -jar shadowsocks-server.jar -s "0.0.0.0:1080" -p "123456" -m "aes-128-cfb"
-s server listen address in format: ipv4:port or [ipv6]:port 
-p Password 
-m encrypt method. support methods: [Stream ciphers: 
                            camellia-128-cfb, salsa20, chacha20-ietf, camellia-192-cfb, camellia-256-cfb, chacha20,
                            aes-128-cfb, aes-256-cfb, rc4-md5, aes-192-cfb, 
                            AEAD ciphers:
                            aes-192-gcm, aes-256-gcm, aes-128-gcm, chacha20-ietf-poly1305 
                            +ss22 spec ciphers:
                            2022-blake3-aes-128-gcm, 2022-blake3-aes-192-gcm, 2022-blake3-aes-256-gcm,]
Currently AEAD ciphers works only with "Neo" client and might not work with other client implementations.

Optional arguments:
 -h                         usage help
 -help                      usage full help
 -bn                        boss thread number
 -wn                        workers thread number
 -ci                        client idle time
 -ri                        remote idle time
 
 ShadowsocksNeo-Client
 
 java -jar shadowsocks-socks.jar -s "127.0.0.1:1088" -p "123456" -m "aes-256-gcm" -c "127.0.0.1:1080"
 
 usage: java -jar shadowsocks-socks.jar -h
 -s,                        server connect address. e.g: ip:port
 -p,                        server password
 -m,                        encrypt method
 -c,                        local expose address. e.g: 127.0.0.1:1080 
                            (put this address on firefox socks v5 proxy configuration)
 -h                         usage help
 -help                      usage full help
 

