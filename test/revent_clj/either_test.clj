(ns revent-clj.either-test
  (:require [clojure.test :refer :all]
            [revent-clj.either :refer :all]))

(def failed (failure :some-error))

(def succeeded (success 5))

(deftest construction
  (testing "construct a successful result"
    (is (= [:value nil] (success :value))))

  (testing "construct a failed result"
    (is (= [nil :error] (failure :error)))))

(deftest fmap-invocation
  (testing "fmap failed value should remain failed"
    (is (= failed (fmap failed inc))))

  (testing "fmap should apply function to successful result"
    (is (= (success 6) (fmap succeeded inc)))))

(deftest bind-invocation
  (testing "bind failed value should remain failed"
    (is (= failed (bind failed (comp success inc)))))

  (testing "bind should apply function to successful result"
    (is (= (success 6) (bind succeeded (comp success inc))))))

(deftest let-err-macro
  (testing "chain successful results"
    (is (= (success 4)
           (let-err
             [foo (success 1)
              bar (success (+ foo 2))]
             (+ foo bar)))))

  (testing "fail fast in case of failure"
    (is (= failed)
        (let-err
          [foo failed
           bar (success (+ foo 2))]
          (+ foo bar)))))
