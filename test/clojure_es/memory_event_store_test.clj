(ns clojure-es.memory-event-store-test
  (:require [clojure.test :refer :all]
            [clojure-es.either :refer :all]
            [clojure-es.memory-event-store :as s]))

(def stream-id 1)

(def ^:dynamic persist-events)

(def ^:dynamic read-events)

(defn is-success [[value error]]
  (nil? error))

(defn setup-store [test]
  (let [store (s/empty-store)
        now (constantly :now)]
    (binding [persist-events (partial s/persist-events store now)
              read-events (partial s/read-events store)]
      (test))))

(use-fixtures :each setup-store)

(deftest persist-event-successfully
  (is (= (success [{:version 1 :payload :some-event :timestamp :now}])
         (persist-events stream-id [:some-event]))))

(deftest persist-multiple-events
  (is (= (success [{:version 1 :payload :event1 :timestamp :now}
                   {:version 2 :payload :event2 :timestamp :now}
                   {:version 3 :payload :event3 :timestamp :now}])
         (persist-events stream-id [:event1 :event2 :event3]))))

(deftest read-empty-event-stream
  (is (= [] (read-events stream-id))))

(deftest read-persisted-event
  (persist-events stream-id [:some-event])
  (is (= [{:version 1 :payload :some-event :timestamp :now}]
         (read-events stream-id))))

(deftest persist-event-with-expected-version
  (persist-events stream-id [:event1 :event2])

  (testing "succeed when expected version matches"
    (is (is-success (persist-events stream-id [:event3] 2))))

  (testing "fail when expected version doesn't match"
    (is (= (failure :concurrent-modification)
           (persist-events stream-id [:other-event] 1)))))
