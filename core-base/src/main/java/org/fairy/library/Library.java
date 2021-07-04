/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.fairy.library;

import lombok.Getter;
import org.fairy.library.relocate.Relocate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Getter
public class Library {
    public static final String IMANITY_LIB_PACKAGE = "org.imanity.framework.libs.";

    public static Library REDISSON = new Library(
            "org{}redisson",
            "redisson-all", // Include all
            "3.13.6",
            "3YN36wajaTShvnJVRh7Q/SyH7HhsZhAIjqxm1vvqQYM=",
            new Relocate("io{}netty", IMANITY_LIB_PACKAGE + "redisson.netty"),
            new Relocate("org{}jboss{}marshalling", IMANITY_LIB_PACKAGE + "redisson.marshalling"),
            new Relocate("org{}xerial{}snappy", IMANITY_LIB_PACKAGE + "redisson.snappy"),
            new Relocate("org{}yaml", IMANITY_LIB_PACKAGE + "redisson.yaml"),
            new Relocate("net{}bytebuddy", IMANITY_LIB_PACKAGE + "redisson.bytebuddy"),
            new Relocate("com{}fasterxml{}jackson", IMANITY_LIB_PACKAGE + "redisson.jackson")
    ),
    YAML = new Library(
            "org{}yaml",
            "snakeyaml",
            "1.20",
            "HOWEuJiOSesajRuXIHGgsg9eyONxV7xakRNP3ZDGEjw="
    ),
    MONGO_DB_SYNC = new Library(
            "org{}mongodb",
            "mongodb-driver-sync",
            "4.2.3",
            "Ve/rvcBxLXAKcDBAuNMNP5I1OIFoMWHCtWWEttetsh0="
    ),
    MONGO_DB_CORE = new Library(
            "org{}mongodb",
            "mongodb-driver-core",
            "4.2.3",
            "WeyB1ROaL4qqyomQfh03xuU/UxsJWsqcQl6xCQXZ65k="
    ),
    MONGOJACK = new Library(
            "org{}mongojack",
            "mongojack",
            "4.2.0",
            ""
    ),
    BSON = new Library(
            "org{}mongodb",
            "bson",
            "4.2.3",
            "yARrppaYx9/Yb0mHglvZPPxwqa98aJ+Muw4V5X3AtK8="
    ),
    CAFFEINE = new Library(
            "com{}github{}ben-manes{}caffeine",
            "caffeine",
            "2.9.0",
            "VFMotEO3XLbTHfRKfL3m36GlN72E/dzRFH9B5BJiX2o=",
            new Relocate("com{}github{}ben-manes{}caffeine", IMANITY_LIB_PACKAGE + "caffeine")
    ),
    GUAVA = new Library(
            "com{}google{}guava",
            "guava",
            "30.0-jre",
            "SIXFTM1H57LSJTHQSY+RW1FY6AQGTA7NKCYL+WEW2IU="
    ),
    FAST_UTIL = new Library(
            "it{}unimi{}dsi",
            "fastutil",
            "8.1.0",
            null
    ),
    BYTE_BUDDY = new Library(
            "net.bytebuddy",
            "byte-buddy",
            "1.10.9",
            "B7nKbi+XDLA/SyVlHfHy/OJx1JG0TgQJgniHeG9pLU0="
    ),
    H2_DRIVER = new Library(
            "com.h2database",
                    "h2",
                    // seems to be a compat bug in 1.4.200 with older dbs
                    // see: https://github.com/h2database/h2database/issues/2078
                    "1.4.199",
                    "MSWhZ0O8a0z7thq7p4MgPx+2gjCqD9yXiY95b5ml1C4="
            // we don't apply relocations to h2 - it gets loaded via
            // an isolated classloader
    ),
    MARIADB_DRIVER = new Library(
            "org{}mariadb{}jdbc",
                    "mariadb-java-client",
                    "2.7.0",
                    "ABURDun85Q01kf119r4yjDtl5ju9Fg9uV2nXyU3SEdw=",
            new Relocate("org{}mariadb{}jdbc", IMANITY_LIB_PACKAGE + "mariadb")
    ),
    MYSQL_DRIVER = new Library(
            "mysql",
                    "mysql-connector-java",
                    "8.0.22",
                    "UBne+9EjFilel6bojyqbB/EYNFpOmCcQu6Iy5JmyL08=",
            new Relocate("com{}mysql", IMANITY_LIB_PACKAGE + "mysql")
    ),
    POSTGRESQL_DRIVER = new Library(
            "org{}postgresql",
                    "postgresql",
                    "9.4.1212",
                    "DLKhWL4xrPIY4KThjI89usaKO8NIBkaHc/xECUsMNl0=",
            new Relocate("org{}postgresql", IMANITY_LIB_PACKAGE + "postgresql")
    ),
    HIKARI = new Library(
            "com{}zaxxer",
                    "HikariCP",
                    "3.4.5",
                    "i3MvlHBXDUqEHcHvbIJrWGl4sluoMHEv8fpZ3idd+mE=",
            new Relocate("com{}zaxxer{}hikari", IMANITY_LIB_PACKAGE + "hikari")
    ),
    SPRING_CORE = new Library(
            "org.springframework",
            "spring-core",
            "5.3.2",
            null
    ),
    SPRING_EL = new Library(
            "org.springframework",
            "spring-expression",
            "5.3.2",
            null
    ),


    ASM = new Library(
            "org.ow2.asm",
                    "asm",
                    "9.1",
                    "zaTeRV+rSP8Ly3xItGOUR9TehZp6/DCglKmG8JNr66I="
    ),
    ASM_COMMONS = new Library(
            "org.ow2.asm",
                    "asm-commons",
                    "9.1",
                    "r8sm3B/BLAxKma2mcJCN2C4Y38SIyvXuklRplrRwwAw="
    ),
    JAR_RELOCATOR = new Library(
            "me.lucko",
                    "jar-relocator",
                    "1.4",
                    "1RsiF3BiVztjlfTA+svDCuoDSGFuSpTZYHvUK8yBx8I="
    );

    private final String mavenRepoPath;
    private final String version;
    private final String name;
    private final byte[] checksum;
    private final List<Relocate> relocations;

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    public Library(String groupId, String artifactId, String version, String checksum, Relocate... relocations) {
        this(groupId, artifactId, version, version, checksum, relocations);
    }

    public Library(String groupId, String artifactId, String versionPackage, String version, String checksum, Relocate... relocations) {
        this.mavenRepoPath = String.format(MAVEN_FORMAT,
                rewriteEscaping(groupId).replace(".", "/"),
                rewriteEscaping(artifactId),
                versionPackage,
                rewriteEscaping(artifactId),
                version
        );
        this.name = artifactId;
        this.version = version;
        if (checksum != null && !checksum.isEmpty()) {
            this.checksum = Base64.getDecoder().decode(checksum);
        } else {
            this.checksum = null;
        }

        this.relocations = new ArrayList<>();
        for (Relocate relocate : relocations) {
            this.relocations.add(new Relocate(rewriteEscaping(relocate.getPattern()), relocate.getShadedPattern()));
        }
    }

    private static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    public String name() {
        return this.name;
    }

    public String getFileName() {
        return this.name.toLowerCase().replace('_', '-') + "-" + this.version;
    }

    public boolean checksumMatches(byte[] hash) {
        if (this.checksum == null) {
            return true;
        }
        return Arrays.equals(this.checksum, hash);
    }

    public static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

}
