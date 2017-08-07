(ns revent-clj.repository-test
  (:require [clojure.test :refer :all]
            [revent-clj.repository :as r]
            [revent-clj.memory-event-store :as s]
            [revent-clj.banking-domain :as d]
            [revent-clj.either :refer :all]))

(def aggregate-id 1)

(def ^:dynamic load-snapshot)

(def ^:dynamic persist-events)

(defn setup-repository [test]
  (let [store (s/empty-store)
        read-events (partial s/read-events store)
        now (constantly :now)]
    (binding [persist-events (partial s/persist-events store now)
              load-snapshot (partial r/load-snapshot read-events d/reducer)]
      (test))))

(use-fixtures :each setup-repository)

(deftest load-empty-snapshot
  (is (= (success {:aggregate {} :version 0})
         (load-snapshot aggregate-id))))

(deftest load-snapshot-from-events
  (persist-events
    aggregate-id
    [(d/->AccountCreated)
     (d/->OwnerChanged "John Doe")
     (d/->DepositPerformed 30)
     (d/->WithdrawalPerformed 10)])

  (testing "succeed when expected version is not specified"
    (is (= (success {:aggregate {:owner "John Doe" :balance 20}
                     :timestamp :now
                     :version 4})
           (load-snapshot aggregate-id))))

  (testing "succeed when expected version matches"
    (is (= (success {:aggregate {:owner "John Doe" :balance 20}
                     :timestamp :now
                     :version 4})
           (load-snapshot aggregate-id 4))))

  (testing "fail when expected version doesn't match"
    (is (= (failure :invalid-aggregate-version)
           (load-snapshot aggregate-id 3)))))
