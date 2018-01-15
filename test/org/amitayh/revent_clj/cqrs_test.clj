(ns org.amitayh.revent-clj.cqrs-test
  (:require [clojure.test :refer :all]
            [org.amitayh.either :refer :all]
            [org.amitayh.revent-clj.cqrs :as cqrs]
            [org.amitayh.revent-clj.memory-event-store :as s]
            [org.amitayh.revent-clj.banking-domain :as d]
            [org.amitayh.revent-clj.repository :as r]))

(def account-id 1)

(def create-command (d/create-account "John Doe" 10))

(def deposit-command (d/deposit 10))

(def ^:dynamic handle)

(def ^:dynamic load-snapshot)

(defn load-account []
  (-> account-id load-snapshot first :aggregate))

(defn last-version [[events error]]
  (if (nil? error)
    (-> events last :version)
    nil))

(defn setup-handler [test]
  (let [store (s/empty-store)
        read-events (partial s/read-events store)
        persist-events (partial s/persist-events store s/now)]
    (binding [load-snapshot (partial r/load-snapshot read-events d/reducer 100)
              handle (fn [command expected-version]
                       (cqrs/handle
                         load-snapshot
                         persist-events
                         (cqrs/->Command account-id command expected-version)))]
      (test))))

(use-fixtures :each setup-handler)

(deftest handle-command-for-new-aggregate
  (handle create-command nil)
  (is (= {:owner "John Doe" :balance 10}
         (load-account))))

(deftest handle-multiple-commands
  (handle create-command nil)
  (handle deposit-command nil)
  (is (= {:owner "John Doe" :balance 20}
         (load-account))))

(deftest fail-if-expected-version-is-wrong
  (let [result (handle create-command nil)
        version (last-version result)
        wrong-expected-version (dec version)]
    (is (= (handle deposit-command wrong-expected-version)
           (failure :invalid-aggregate-version)))))

(deftest succeed-if-expected-version-is-correct
  (let [result (handle create-command nil)
        version (last-version result)]
    (handle deposit-command version)
    (is (= {:owner "John Doe" :balance 20}
           (load-account)))))

(deftest fail-if-command-is-rejected
  (handle create-command nil)
  (is (= (handle (d/withdraw 20) nil)
         (failure :insufficient-funds))))
