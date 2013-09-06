(ns vdd-core.data
  "This is an experimental helper for allowing visualization of arbitrary data as HTML unordered lists.
  Unused currently."
  (:use [vdd-core.util :only [log]]
        [clojure.string :only [join]])
  (:require [vdd-core.connection :as connection]))

(defprotocol DataToHtml
  "Defines a method for converting from generic data to HTML structures"
  (data->html [data] "Converts the data to html"))

(def target-element (atom nil))

(defn visualize-data
  "Callback for visualizing data "
  [topic data]
  (.html @target-element (data->html (js->clj data))))

(defn ^:export enableDataView
  "Enables viewing on an element. When data is received for viewing it will be displayed in the given target."
  [new-target]
  (reset! target-element new-target)
  (connection/connect visualize-data))

(extend-protocol DataToHtml
  cljs.core.PersistentHashMap
  (data->html [m]
    (str "<ul>"
         (join (map (fn [[k v]] (str "<li>" k "&nbsp;-&nbsp;" (data->html v) "</li>")) m))
         "</ul>"))

  cljs.core.PersistentVector
  (data->html [array]
    (str "<ul>"
         (join (map #(str "<li>" (data->html %) "</li>") array))
         "</ul>"))

  string
  ; String represented as itself
  (data->html [s] s)

  number
  ; Number represented as itself
  (data->html [n] (str n))

  object
  ; Other objects converted to json
  (data->html [obj] (JSON/stringify obj)))
