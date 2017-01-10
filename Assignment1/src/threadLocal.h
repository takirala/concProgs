#ifndef THREADLOCAL_H_
#define THREADLOCAL_H_

#include <iostream>
#include <map>
#include <stdexcept>
#include <mutex>

using namespace std;

namespace cop5618 {

    template<typename T>
    class threadLocal {
    public:
        threadLocal();

        ~threadLocal();

        //disable copy, assign, move, and move assign constructors
        threadLocal(const threadLocal &) = delete;

        threadLocal &operator=(const threadLocal &)= delete;

        threadLocal(threadLocal &&) = delete;

        threadLocal &operator=(const threadLocal &&)= delete;

        /**
        * Returns a reference to the current thread's value.
        * If no value has been previously set by this
        * thread, an out_of_range exception is thrown.
        *
        */
        const T &get() const;

        /**
        * Sets the value of the threadLocal for the current thread
        * to val.
        */
        void set(T val);

        /**
        * Removes the current thread's value for the threadLocal
        */
        void remove();

        /**
        * Friend function. Useful for debugging only, shows values for all threads.
        */
        template<typename U>
        friend std::ostream &operator<<(std::ostream &os, const threadLocal<U> &obj);

    private:
        //ADD PRIVATE MEMBERS HERE
        map<thread::id, T> threadLocals;

        /**
         * We will use this mutex to guard all accesses to data.
         *
         * The mutex, which is marked mutable so that methods that do not modify the
         * object except in acquiring and releasing the lock (for example, size and empty)
         * can be marked as const.
         */
        mutable std::mutex m;  //a mutex.

    };

    //ADD DEFINITIONS; ADD IMPLEMENTATIONS OF THE PUBLIC METHODS AND THE ≪ OVERLOAD HERE

    template<typename T>
    threadLocal<T>::threadLocal(void) {}

    template<typename T>
    threadLocal<T>::~threadLocal(void) {
        threadLocals.clear();
    }

    template<typename T>
    void threadLocal<T>::set(T value) {
        lock_guard<std::mutex> lock(m);
        // Update if not already present. Upsert!
        this->threadLocals[this_thread::get_id()] = value;
    }


    template<typename T>
    const T &threadLocal<T>::get() const {
        thread::id key = this_thread::get_id();
        if (threadLocals.find(key) == threadLocals.end())
            throw std::invalid_argument("[SET] This thread has not yet initialized the thread Local variable!");
        return this->threadLocals.at(this_thread::get_id());
    }

    template<typename T>
    void threadLocal<T>::remove() {
        thread::id key = this_thread::get_id();
        if (threadLocals.find(key) == threadLocals.end())
            throw std::invalid_argument("[REMOVE] This thread has not yet initialized the thread Local variable!");
        lock_guard<std::mutex> lock(m);
        this->threadLocals.erase(this_thread::get_id());
    }

} /* namespace cop5618 */
#endif /* THREADLOCAL_H_ */