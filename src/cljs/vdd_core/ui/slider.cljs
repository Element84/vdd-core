(ns vdd-core.ui.slider
  "A very small wrapper around jquery ui slider
  http://jqueryui.com/slider
  http://api.jqueryui.com/slider"
  (:use [vdd-core.util :only [log]]))

(defn create
  "Creates a slider within the element given. Returns the new slider.
  Takes a slide-callback function which will take the new value of the slider."
  ([element slide-callback]
   (create element slide-callback {:min 0 :value 0 :step 1 :max 0}))
  ([element slide-callback slider-options]
    (let [element (js/$ element)
          callback-wrapper (fn [event ui]
                             (slide-callback (.-value ui)))
          slider-options (assoc slider-options :slide callback-wrapper)]
      (.slider element (clj->js slider-options)))))

(defn set-option!
  "Sets one of the options on the slider"
  [slider option value]
  (.slider slider "option" option value))

(defn set-value!
  "Sets the slider value"
  [slider value]
  (.slider slider "value" value))
