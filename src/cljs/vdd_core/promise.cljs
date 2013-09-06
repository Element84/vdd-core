(ns vdd-core.promise
  "A (very) poor man's implementation of promises in clojurescript. Once core.async becomes more primetime we
  should switch this to use that."
  (:use [vdd-core.util :only [log set-timeout]]))


(def no-value-set-marker :no-value-set)
(def WAIT_TIME 100)
(def PROMISE_TIMEOUT 10000)

(defn promise
  "Returns a new promise object. Can be set with deliver and dereferenced with the deref-wait command in this package.
  The promise returned is actually just an atom."
  []
  (atom no-value-set-marker))

(defn deliver
  "Sets the value on the promise, releasing any pending derefs.
  A subsequent call to deliver on a promise will have no effect."
  [p value]
  (when (= no-value-set-marker @p)
    (reset! p value)))

(defn deref-then
  "Dereferences the promise then executes the given function. If the promise times out the timeout-handler will be invoked"
  ([p f timeout-handler]
   (deref-then p f timeout-handler 0))
  ([p f timeout-handler time-waited]
   (let [v @p]
     (if (= no-value-set-marker v)
       (if (> time-waited PROMISE_TIMEOUT)
         (timeout-handler)
         (set-timeout
           #(deref-then p f timeout-handler (+ time-waited WAIT_TIME))
           WAIT_TIME))
       (f v)))))