(ns vdd-core.connection
  "Handles connecting to WAMP, receiving data, and making RPC calls."
  (:require [vdd-core.promise])
  (:use [vdd-core.util :only [log js-obj->map]]))

(def base-topic-uri "http://vdd-core/")
(def connect-options {:maxRetries 60 :retryDelay 30000})

(defn- session-connection-handler
  "Handles session connections. Configures the session and sets up the vizdata-handler as the function that is called
  when visualization data arrives."
  [channel-config session]
  (log (str "Connected with session " (.sessionid session)))
  ;  Add CURI prefixes
  (.prefix session "event" (str base-topic-uri "event#"))
  (.prefix session "rpc"   (str base-topic-uri "rpc#"))
  (doseq [[channel handler] channel-config]
    (.subscribe session
                (format "event:%s"
                        (if (keyword? channel)
                          (name channel)
                          channel))
                handler)))

(defn- disconnect-handler
  "Handles disconnections from the server."
  [code reason]
  (when (not= 0 code)
    (log ("Connection lost (" reason ")"))))

(defn- channels-to-handlers-or-callback->config
  "Takes an argument to 'connect' which could be either a javascript map of channel names to callback functions
  or a single callback function for normal vizdata. If it's a javascript map it converts it to a clojure map.
  If it's a single function it puts it in a clojure map associated with :vizdata."
  [channels-to-handlers-or-callback]
  (if (fn? channels-to-handlers-or-callback)
    {:vizdata channels-to-handlers-or-callback}
    (js-obj->map channels-to-handlers-or-callback)))

(defn ^:export connect
  "Starts up a connection to the wamp server using the autobahn.js library.
  channels-to-handlers should be either a javascript map of channel names to callback functions
  or a single callback function for normal vizdata.
  Returns a 'session' object that should be used with other operations such as sendData."
  [channels-to-handlers-or-callback]
  (let [channel-config (channels-to-handlers-or-callback->config channels-to-handlers-or-callback)
        ws-uri (format "ws://localhost:%d/ws" (or (:port channel-config) (.-port js/location)))
        session-promise (vdd-core.promise/promise)
        connection-handler (fn [session]
                             (session-connection-handler channel-config session)
                             (vdd-core.promise/deliver session-promise session))]
    (.connect js/ab
              ws-uri
              connection-handler
              disconnect-handler
              connect-options)
    session-promise))

(defn call-server-fn
  "Invokes a function on the servers side with data from the visualization. The data sent can be any JSON serializable
  object. Takes the following arguments:
  * session - the session object returned from calling connect.
  * server-data-fn - A string containing the namespace and function to call on server side. ie. 'my-ns/test-code'
  * data - The data to send
  * success - optional callback function with the response data. Defaults to logging responses.
  * error - optional callback function to handle errors. Defaults to logging errors."
  ([session-promise server-data-fn data]
    (call-server-fn session-promise
               server-data-fn
               data
               {:success (fn [data] (log (str "Successfully called and received " data)))
                :error (fn [data] (log (str "Error sending data from viz " data)))}))
  ([session-promise
    server-data-fn
    data
    {success-handler :success
     error-handler :error}]
   (vdd-core.promise/deref-then
     session-promise
     (fn [session]
       (if session
         (do
           (log (format "Calling function %s with data %s" server-data-fn data))
           (.then (.call session "rpc:data-callback" (clj->js {:fn server-data-fn :data data}))
                  success-handler
                  error-handler))
         (log "The session object was nil. We don't appear to be connected.")))
     #(do
        (log "Timed out waiting for session object while trying to call server function.")
        (error-handler "timeout")))))

(defn ^:export callServerFunction
  "Public version of call-server-fn for JavaScript interoperability. It handles javascript options object."
  ([session-promise server-data-fn data]
   (call-server-fn session-promise server-data-fn data))
  ([session-promise server-data-fn data js-options]
   (log (format "Using options %s" js-options))
   (call-server-fn session-promise server-data-fn data (js-obj->map js-options))))


