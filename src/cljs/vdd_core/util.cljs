(ns vdd-core.util)

; Figure out if we're running in the rhino repl
(def in-rhino (empty? (filter
                     #(= "document" %)
                     (js->clj
                       (.keys js/Object
                              (js/eval "this"))))))

(defn log
  "Logs the string to the console or using println."
  [str]
  (if in-rhino
    (println str)
    (.log js/console str)))

(defn js-obj->map
  "Converts a javascript object to a map with keyword keys. Does not handle subobjects"
  [jo]
  (into {} (for [[k v] (js->clj jo)]
             [(keyword k) v])))

(defn set-timeout
  "Invokes the function f in the given number of milliseconds. A wrapper around window.setTimeout"
  [f milliseconds]
  (.setTimeout js/window f milliseconds))