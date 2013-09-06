(ns vdd-core.main
  "This is used when running vdd-core from the command line. The main purpose of this is when using
  it with non-java platforms. With Java platforms we expect that we'll be able to directly integrate
  it into the running platform some how. With other platforms we need a way to incorporate the two
  of them together. This allows vdd-core to be started and communicated with using stdout and stdin."
  (:gen-class)
  (:require [vdd-core.core :as core]
            [cheshire.core :as cheshire]))

(defn- parse-command
  "Parses an input line into a command. The input line is split on the first space. To the left of the
  space is the command. The right of the space is the data (if there is any)."
  [input]
  (let [parts (clojure.string/split input #"\s" 2)]
    {:command (first parts)
     :data (nth parts 1 nil)}))

(defn- execute-command [cmd data]
  (cond
    (= cmd "vizdata")
    (let [parsed (cheshire/parse-string data)]
      (core/data->viz parsed))
    :else (println "Unknown command: " cmd)))

(defn- read-loop []
  (loop [input (read-line)]
    (let [{cmd :command
          data :data} (parse-command input)]
      (when-not (= "exit" cmd)
        (execute-command cmd data)
        (recur (read-line))))))

(defn -main [& args]
  (let [config (core/config)
        config (assoc-in config [:log :stdout-enabled] false)
        server (core/start-viz config)]
    (println "Ready for input")
    (read-loop)
    (core/stop-viz server)))