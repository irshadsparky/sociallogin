# Social Login
All in one social Login

How to
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

gradle
maven
sbt
leiningen
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			  url  "https://dl.bintray.com/ir9977/SocialLogin"
		}
	}
Step 2. Add the dependency

	dependencies {
	            implementation 'com.sociallogin:library:0.3'
	}
