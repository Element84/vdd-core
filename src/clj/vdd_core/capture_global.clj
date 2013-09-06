(ns vdd-core.capture-global
  "Allows capturing code execution data from code being visualized. This is a very simplistic
  implementation that stores captured state in a vector in an atom."
  (:require [taoensso.timbre :as timbre :refer (debug)]))

(def ^:private _captured (atom []))

(def ^:private enabled (atom false))

(defn enable
  "Enables capturing of data. This should only be done in development mode.
  Initially enabled is false so no data will be captured."
  ([]
   (enable true))
  ([do-enable]
   (reset! enabled do-enable)))

(defn captured
  "Returns the data that was captured."
  []
  @_captured)

(defn reset-captured!
  "Clear the captured state."
  []
  (reset! _captured []))

(defn capture!
  "Captures data for visualization."
  [data]
  (when @enabled
    (debug "Capturing " data)
    (swap! _captured concat [data])))