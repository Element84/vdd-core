(ns vdd-core.internal.wamp-handler
  "Handles connecting and receiving data over the WAMP protocol."
  (:require [taoensso.timbre :as timbre
             :refer (trace debug info warn error fatal spy)]
            [org.httpkit.server :as http-kit]
            [clj-wamp.server :as wamp]))

;; Topic BaseUrls
(def base-url "http://vdd-core")
(def rpc-base-url (str base-url "/rpc#"))
(def evt-base-url (str base-url "/event#"))

(defn rpc-url [path] (str rpc-base-url path))
(defn evt-url [path] (str evt-base-url path))

;; HTTP Kit/WAMP WebSocket handler

(defn- on-open [sess-id]
  (debug "WAMP client connected [" sess-id "]"))

(defn- on-close [sess-id status]
  (debug "WAMP client disconnected [" sess-id "] " status))

(defn- to-string
  "Converts s to a string. If s is a keyword it will return its name."
  [s]
  (if (keyword? s)
    (name s)
    (str s)))

(defn- wamp-config [config]
  (let [channel-config (into {} (for [channel (:data-channels config)]
                                  [(evt-url (to-string channel)) true]))
        rpc-config (into {}(for [[rpc-name f] (:viz-request-handlers config)]
                             [(rpc-url (to-string rpc-name)) f]))]
    {:on-open        on-open
     :on-close       on-close
     :on-call        rpc-config
     :on-subscribe   channel-config
     :on-publish     channel-config}))

(defn handler
  "Returns a http-kit websocket handler with wamp subprotocol"
  [config req]
  (let [ws-origins-re (re-pattern (str "http://localhost:" (:port config)))]
    (wamp/with-channel-validation
      req channel ws-origins-re
      (wamp/http-kit-handler channel (wamp-config config)))))

