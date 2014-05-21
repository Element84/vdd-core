(ns vdd-core.internal.system
  (:require [org.httpkit.server :as httpkit]
            [ring.middleware.reload :as ring-reload]
            [clojure.java.io :as io]
            [vdd-core.internal.routes :as routes]
            [taoensso.timbre :as timbre
             :refer (trace debug info warn error fatal spy)])
  (:import [java.io
            PrintStream
            OutputStream]))

(defn- silence-std-out
  "Quiets standard out and standard error while executing the function"
  [f]
  (let [orig-out System/out
        orig-err System/err
        nothing-writer (PrintStream. (proxy [OutputStream] []
                                       (write
                                         ([b]
                                              ;; does nothing
                                              )

                                       ([_ _ _]
                                              ;;does nothing
                                              ))
                                       ))]
    (System/setOut nothing-writer)
    (System/setErr nothing-writer)
    (try
      (f)
      (finally
        (System/setOut orig-out)
        (System/setErr orig-err)))))

(defn- stop-server [server]
  (when-not (nil? server)
    (info "stopping server...")
    ;; server contains the stop-server callback fn
    ;; Hide the annoying java.util.concurrent.RejectedExecutionException displayed by HTTP Kit during restarts.
    ;; See https://github.com/http-kit/http-kit/issues/91
    ;; I tried all solutions but couldn't get any other way to work.
    (silence-std-out #(server :timeout 2000))))

(defn- start-server [config]
  (let [http-config {:ip "0.0.0.0"
                     :port (:port config)
                     :thread 2}
        the-app (ring-reload/wrap-reload (routes/app config))
        server (httpkit/run-server the-app http-config)]
    (info "server started. listen on" (:ip http-config) "@" (:port http-config) "with config" (pr-str http-config))
    server))

(defn system
  "Returns a new instance of the whole application."
  [config]
  {:pre [(:port config)
         (:viz-root config)]}
  {:config config
   :server nil})

(defn- setup-logging [config]
  (let [log-config (:log config)]
    (timbre/set-level! (or (:level log-config) :warn))
    (timbre/set-config! [:timestamp-pattern] "yyyy-MM-dd HH:mm:ss")

    ; Enable file logging
    (timbre/set-config! [:appenders :spit :enabled?] true)
    (timbre/set-config! [:shared-appender-config :spit-filename]
                        (:file log-config))

    ; Enable/disable stdout logs
    (timbre/set-config! [:appenders :standard-out :enabled?]
                        (:stdout-enabled log-config))))

(defn start
  "Performs side effects to initialize the system, acquire resources,
  and start it running. Returns an updated instance of the system."
  [system]
  (setup-logging (:config system))
  (info "Viz Server Starting")
  (assoc-in system [:server] (start-server (:config system))))

(defn stop
  "Performs side effects to shut down the system and release its
  resources. Returns an updated instance of the system."
  [system]
  (info "Viz Server Stopping")
  (stop-server (:server system))
  (assoc system :server nil))