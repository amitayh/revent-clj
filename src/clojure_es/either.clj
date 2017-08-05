(ns clojure-es.either)

(defn success [value] [value nil])

(defn failure [error] [nil error])

(defn fmap [[value error :as either] f]
  (if (nil? error) (success (f value)) either))

(defn bind [[value error :as either] f]
  (if (nil? error) (f value) either))

(defmacro let-err [bindings body]
  (if (= (count bindings) 2)
    `(fmap
       ~(second bindings)
       (fn [~(first bindings)] ~body))
    (let [first-pair (take 2 bindings)
          other-pairs (vec (drop 2 bindings))]
      `(bind
         ~(second first-pair)
         (fn [~(first first-pair)]
           (let-err ~other-pairs ~body))))))
