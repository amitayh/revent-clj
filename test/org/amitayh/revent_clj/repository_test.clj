(ns org.amitayh.revent-clj.repository-test
  (:require [clojure.test :refer :all]
            [org.amitayh.either :refer :all]
            [org.amitayh.revent-clj.snapshot :refer :all]
            [org.amitayh.revent-clj.banking-domain :refer :all]
            [org.amitayh.revent-clj.repository :as r]
            [org.amitayh.revent-clj.memory-event-store :as s]))

(def aggregate-id 1)

(def ^:dynamic load-snapshot)

(def ^:dynamic persist-events)

(defn setup-repository [test]
  (let [store (s/empty-store)
        read-events (partial s/read-events store)
        now (constantly :now)]
    (binding [persist-events (partial s/persist-events store now)
              load-snapshot (partial r/load-snapshot read-events reducer 2)]
      (test))))

(use-fixtures :each setup-repository)

(deftest load-empty-snapshot
  (is (= (success (->Snapshot {} 0 nil))
         (load-snapshot aggregate-id))))

(deftest load-snapshot-from-events
  (persist-events
    aggregate-id
    [(->AccountCreated)
     (->OwnerChanged "John Doe")
     (->DepositPerformed 30)
     (->WithdrawalPerformed 10)])

  (testing "succeed when expected version is not specified"
    (is (= (success (->Snapshot {:owner "John Doe" :balance 20} 4 :now))
           (load-snapshot aggregate-id))))

  (testing "succeed when expected version matches"
    (is (= (success (->Snapshot {:owner "John Doe" :balance 20} 4 :now))
           (load-snapshot aggregate-id 4))))

  (testing "fail when expected version doesn't match"
    (is (= (failure :invalid-aggregate-version)
           (load-snapshot aggregate-id 3)))))
