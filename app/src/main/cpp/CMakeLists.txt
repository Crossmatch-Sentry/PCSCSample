# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# configure import libs
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../distribution)

# shared lib will also be tucked into APK and sent to target
# refer to app/build.gradle, jniLibs section for that purpose.
# ${ANDROID_ABI} is handy for our purpose here. Probably this ${ANDROID_ABI} is
# the most valuable thing of this sample, the rest are pretty much normal cmake
add_library(lib_pcsclite SHARED IMPORTED)
set_target_properties(lib_pcsclite PROPERTIES IMPORTED_LOCATION
    ${distribution_DIR}/pcsclite/lib/${ANDROID_ABI}/libpcsclite.so)

# build application's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")


# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             pcsc_jni

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             PcscExceptions.cpp
             PcscJni.cpp
             )

target_include_directories(pcsc_jni PRIVATE
                           ${distribution_DIR}/pcsclite/include
                           )

target_link_libraries(pcsc_jni
                      android
                      lib_pcsclite
                      log)

