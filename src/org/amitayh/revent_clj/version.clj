(ns org.amitayh.revent-clj.version)

(defn validate [expected actual]
  (or (nil? expected)
      (= expected actual)))
