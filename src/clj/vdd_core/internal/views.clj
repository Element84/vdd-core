(ns vdd-core.internal.views
  (:use [hiccup.core]
        [hiccup.element :only [javascript-tag]]
        [hiccup.page :only [html5 include-css include-js]]))

(defn- navbar 
  "Creates a navbar for vdd page. Takes a list of visualization tuples to link to which
  should contain a path and title."
  [vizs]
  [:div.navbar.navbar-inverse.navbar-fixed-top
   [:div.navbar-inner
    [:div.container
     [:button {:type "button" 
               :class "btn btn-navbar" 
               :data-toggle "collapse" 
               :data-target ".nav-collapse"}
      [:span.icon-bar ]
      [:span.icon-bar ]
      [:span.icon-bar ]]
     [:a {:class "brand" :href "/"} "VDD" ]
     [:div.nav-collapse.collapse
      [:ul.nav
       [:li.active [:a {:href "/"} "Home"]]
       [:li [:a {:href "http://visualizationdrivendevelopment.com"} "About"]]
       [:li.dropdown
        [:a {:href "#" :class "dropdown-toggle" :data-toggle "dropdown"} "Visualizations" [:b.caret]]
        [:ul.dropdown-menu
         [:li.nav-header "Built in" ]
         (for [{path :path title :title} (:built-in vizs)]
           [:li [:a {:href path} title]])
         [:li.divider ]
         [:li.nav-header "Project" ]
         (for [{path :path title :title} (:project vizs)]
           [:li [:a {:href path} title]])]]]]]]])

(defn- head [title]
  [:head 
     [:meta {:charset "utf-8"}]
     [:title title]
     [:meta {:name "viewport" 
             :content "width=device-width, initial-scale=1.0"}]
     (include-css "/bootstrap/css/bootstrap.min.css" 
                  ; Included before bootstrap-responsive because we want it
                  ; to override the body padding top and bottom styles in smaller pages
                  "/vdd/vdd.css"
                  "/bootstrap/css/bootstrap-responsive.min.css" 
                  )])

(defn- footer []
  [:div.container
    [:footer 
     [:hr]
     [:p "&copy; Jason Gilman 2013"]]])

(defn- vdd-page 
  "Creates a visualization page taking the page title, the vizs to link to in the
  navbar and the page contents."
  [& {title :title vizs :vizs content :content javascripts :javascripts}]
  (html5 
    (head title)
    (vec (concat [:body 
           (navbar vizs)
           content
           (footer)
           (include-js "/jquery/jquery.min.js"
                       "/bootstrap/js/bootstrap.min.js"
                       "/autobahn/autobahn.min.js"
                       "/vdd/vdd-core.js")]
          javascripts))))

(defn- project-visualizations
  "Finds the visualizations available in the configured :viz-root."
  [config]
  (let [viz-root (:viz-root config)]
    ; Look in viz-root ...
    (->> viz-root
         clojure.java.io/file 
         ; ... for all directories (recursively)
         file-seq 
         (map str)
         ; ... that contain an index.html.
         (filter #(re-seq #"/index.html$" %))
         ; Get the directory name
         (map #(-> (str viz-root "/(.*)/index.html$")
                   re-pattern
                   (re-matches %)
                   last))
         ; Return the path and title of the visualizations
         (map (fn [viz] {:path (format "/viz/%s" viz) 
                      :title viz})))))

(defn- visualizations [config]
  {:built-in [{:path "/built-in/data-viewer" :title "Data Viewer"}]
   :project (project-visualizations config)})

(defn list-views-page
  "Returns a page containing the visualizations available"
  [config]
  (let [vizs (visualizations config)
        viz-link-fn (fn [{path :path title :title}]
                      [:a.btn.btn-large.span5 {:href path} title])]
    (vdd-page :title "Visualization Driven Development - Core"
              :viz vizs
              :content [:div.container 
                        [:div.hero-unit
                         [:h1 "Visualization Driven Development"]
                         [:p "TODO description here"]]
                        [:div.row
                         [:div.span6
                          [:h3 "Built In Visualizations"]
                          (for [viz (:built-in vizs)]
                            (viz-link-fn viz))]
                         [:div.span6
                          [:h3 "Project Visualizations"]
                          (for [viz (:project vizs)]
                            (viz-link-fn viz))]]])))

(defn data-viewer-page
  "Returns a visualization page that allows arbitrary data to be viewed"
  [config]
  (let [vizs (visualizations config)]
    (vdd-page :title "VDD - Data Viewer"
              :vizs vizs
              :content [:div.container
                        [:h1 "Data Viewer"]
                        [:p "TODO some text here describing the data viewer and that it allows you to show arbitrary data in the browser."]
                        [:p "TODO some code here showing an example"]
                        [:div#target "Data will appear here."]]
              :javascripts [(javascript-tag "$(function() { 
                                            vdd.data.enableDataView($('div#target')); 
                                            });")])))
