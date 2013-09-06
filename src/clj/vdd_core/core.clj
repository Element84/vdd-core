(ns vdd-core.core
  "Contains the core functions for starting and stopping the visualization server and sending
  data to the visualization."
  (:require [vdd-core.internal.system :as vsystem]
            [clj-wamp.server :as wamp]
            [clojure.string]
            [taoensso.timbre :as timbre
             :refer (trace debug info warn error fatal spy)]
            [vdd-core.internal.wamp-handler :as wamp-handler]))

(defn- handle-viz-call
  "Handles rpc calls from the visualization calling the function specified by 'fn' with the data."
  [{data-handler "fn" data "data"}]
  (info "Received data " data)
  (try
    (let [[handler-ns handler-fn] (clojure.string/split data-handler #"/")]
      (require (symbol handler-ns))
      (let [data-handler (resolve (read-string data-handler))]
        (data-handler data)))
    (catch Exception e
      (error e "Error trying to invoke data handler"))))

(defn config
  "Creates a new default config map and returns it. Options:
  * :port - The port the visualization server will run on. Default: 8080.
  * :viz-root - The directory in your project that custom visualizations will be found in.
                 Default: viz
  * :data-channels - A list of data channels over which visualization data or other data can be sent.
                     Default: [\"vizdata\"]
  * :viz-request-handlers - A map of rpc urls to functions that will handle requests. Typically the
                            default data-callback is sufficient as it can execute any function with
                            data sent by the visualization.
  * :log - a map of log settings
    * :level - Default: debug
    * :file - Default: log/vdd-core.log
    * :stdout-enabled - Log messages will appear in stdout. IE the repl window. Default: true
  "
  []
  {:port 8080
   :viz-root "viz"
   :data-channels ["vizdata"]
   :viz-request-handlers {"data-callback" handle-viz-call}
   :log {:level :debug
         :file "log/vdd-core.log"
         :stdout-enabled true}})

(defn start-viz
  "Starts the visualization server and returns an instance of it. Takes a config map as returned by
  `config`."
  ([]
   (start-viz (config)))
  ([config]
   (vsystem/start (vsystem/system config))))

(defn stop-viz
  "Stops the visualization server."
  [server]
  (vsystem/stop server)
  nil)

(defn data->viz
  "Sends the captured data to the visualization. A channel can be specified or it will default to
  \"vizdata\""
  ([data] (data->viz "vizdata" data))
  ([channel data]
    (wamp/send-event! (wamp-handler/evt-url channel) data)))
