//
// Created by takirala on 10/3/2016.
//

#include <iostream>
#include <vector>
#include <thread>
#include <algorithm>
#include "threadLocal.h"
#include <atomic>
#include <list>

using namespace cop5618;
using namespace std;

/**
 * Creates @param num_of_threads concurrently and check the integrity of threadLocal variable.
 */
void test_threadLocal_0(int num_of_threads);

/**
 * Checks the threadLocal with different types of variables.
 */
void test_threadLocal_1(int num_of_threads);

/**
 * Uninitialized variable must throw an error. Attempt to read and remove non existing values.
 */
void test_threadLocal_2();

/**
 * Test parallel reads, writes and removes. A read after remove should throw an exception.
 */
void test_threadLocal_3(int num_of_threads);

/**
 * Helper functions
 */
void threadLocal_assign(int index, int value);

void threadLocal_type_check(float requestedFloat, long requestedLong, bool requestedBool);

void thread_set_get_remove(int value);

std::atomic<int> test_threadLocal_0_err;
std::atomic<int> test_threadLocal_1_err;
std::atomic<int> test_threadLocal_2_err;
std::atomic<int> test_threadLocal_3_err;

// Int variable
threadLocal<int> threadLocal_int;
// Test float variable
threadLocal<float> threadLocal_float;
// Test double variable
threadLocal<long> threadLocal_long;
// Test boolean variable
threadLocal<bool> threadLocal_bool;

int test_threadLocal() {
    clock_t begin = clock();
    int num_errs = 0;

    int num_of_threads = 10000;

    cout << "Started test suite" << endl;

    test_threadLocal_0(num_of_threads);
    test_threadLocal_1(num_of_threads);
    test_threadLocal_2();
    test_threadLocal_3(num_of_threads);

    cout << "Test 0 -> " << test_threadLocal_0_err << " errors!"
         << "\nTest 1 -> " << test_threadLocal_1_err << " errors!"
         << "\nTest 2 -> " << test_threadLocal_2_err << " errors!"
         << "\nTest 3 -> " << test_threadLocal_3_err << " errors!" << endl;

    num_errs += test_threadLocal_0_err + test_threadLocal_1_err + test_threadLocal_2_err + test_threadLocal_3_err;

    double elapsed_secs = double(clock() - begin) / CLOCKS_PER_SEC;
    cout << "Number of errors " << num_errs << " Time :" << elapsed_secs << endl;
    return num_errs;
}

vector<int> source;
vector<int> destination;

void test_threadLocal_0(const int num_of_threads) {
    vector<thread> threads;
    int random = rand() * 10000;
    for (int i = 0; i < num_of_threads; ++i) {
        source.push_back(random + i);
        destination.push_back(0);
    }

    // Create all the threads.
    for (int index = 0; index < num_of_threads; index++) {
        threads.push_back(std::thread(threadLocal_assign, index, random + index));
    }

    // Join all the threads.
    for (auto &t : threads) {
        t.join();
    }

    // Make sure all the threads have counter value as designed.
    for (int i = 0; i < num_of_threads; ++i) {
        if (source[i] != destination[i]) {
            test_threadLocal_0_err++;
        }
    }
}

void threadLocal_assign(int index, int value) {
    threadLocal_int.set(value);
    destination[index] = threadLocal_int.get();
}

void test_threadLocal_1(const int num_of_threads) {

    vector<thread> threads;
    test_threadLocal_1_err = 0;
    for (int i = 0; i < num_of_threads; i++) {
        float randFloat = static_cast <float> (rand()) / (static_cast <float> (RAND_MAX / 100000));
        long randLong = static_cast <long> (rand()) / (static_cast <long> (RAND_MAX / 100000));
        bool randBool = rand() & 1;
        //cout << randFloat << " - " << randLong << " - " << randBool << endl;
        threads.push_back(std::thread(threadLocal_type_check, randFloat, randLong, randBool));
    }
    for (auto &t : threads) {
        t.join();
    }
    return;
}

void threadLocal_type_check(float requestedFloat, long requestedLong, bool requestedBool) {
    threadLocal_float.set(requestedFloat);
    threadLocal_long.set(requestedLong);
    threadLocal_bool.set(requestedBool);

    if (threadLocal_float.get() != requestedFloat || threadLocal_bool.get() != requestedBool ||
        threadLocal_long.get() != requestedLong) {
        test_threadLocal_1_err++;
    }
}

void test_threadLocal_2() {
    threadLocal<int> local;

    try {
        local.get();
        test_threadLocal_2_err++;
    } catch (invalid_argument ex) {
        // do nothing as exception is desired
    }

    try {
        local.remove();
        test_threadLocal_2_err++;
    } catch (invalid_argument ex) {
        // do nothing as exception is desired
    }
}

void test_threadLocal_3(int num_of_threads) {
    vector<thread> threads;
    test_threadLocal_3_err = 0;
    for (int i = 0; i < num_of_threads; i++) {
        srand(time(0));
        int value = rand() * 10000;
        thread_set_get_remove(value);
    }
    for (auto &t : threads) {
        t.join();
    }
}

void thread_set_get_remove(int value) {
    threadLocal_int.set(value);

    if (rand() & 1) {
        //set multiple times.
        threadLocal_int.set(value);
    }

    if (threadLocal_int.get() != value) {
        test_threadLocal_3_err++;
        return;
    }

    threadLocal_int.remove();

    try {
        threadLocal_int.get();
    } catch (invalid_argument ex) {
        return;
    }
    test_threadLocal_3_err++;
}