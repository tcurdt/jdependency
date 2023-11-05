package nl.example;
/*
 * Copyright 2010-2023 The jdependency developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import nl.basjes.maven.multijdk.JavaVersion;
import nl.basjes.maven.multijdk.App;

public class Main {
    public static void main(String[] args) {
        JavaVersion javaVersion = new JavaVersion();
        System.out.println("Java detect: " + javaVersion.getCodeVersion());
        System.out.println("Java major : " + javaVersion.getJavaMajorVersion());
        System.out.println("App code   : " + new App().doSomething());
    }
}