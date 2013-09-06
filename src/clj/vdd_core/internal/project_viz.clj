(ns vdd-core.internal.project-viz
  "Handles finding visualizations in a user project."
  (:require [clojure.java.io :as io]))

(defn- viz-dirs
  "Gets subdirectories of the viz-root that have an index.html"
  [config]
  (let [viz-root (:viz-root config)]
    ; Look in viz-root ...
    (->> viz-root
         io/file
         ; ... for all directories (recursively)
         file-seq
         (map str)
         ; ... that contain an index.html.
         (filter #(re-seq #"/index.html$" %))
         ; Get the directory name
         (map #(-> (str viz-root "/(.*)/index.html$")
                   re-pattern
                   (re-matches %)
                   last)))))

(defn project-visualizations
  "Finds the visualizations available in the configured :viz-root."
  [config]
  (let [viz-root (:viz-root config)
        vizs (viz-dirs config)]
    ; Return the path and title of the visualizations
    (map (fn [viz]
           (let [path (format "%s/%s" viz-root viz)
                 driver (->> path
                             io/file
                             file-seq
                             (map str)
                             (filter #(.startsWith % (format "%s/driver." path)))
                             first)]
             {:path path
              :title viz
              :driver driver}))
           vizs)))

