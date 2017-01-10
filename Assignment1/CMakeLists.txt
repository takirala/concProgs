cmake_minimum_required(VERSION 3.6)
project(CpHomework1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES src/HW1.cpp src/threadLocal.h src/test_threadLocal.cpp)

add_executable(CpHomework1 ${SOURCE_FILES})