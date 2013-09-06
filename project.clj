(defproject vdd-core "0.1.0"
  :description "vdd-core is a minimal Clojure library built to help enable Visualization Driven Development (VDD)."
  :url "https://github.com/Element84/vdd-core"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}

  :main vdd-core.main

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.taoensso/timbre "2.6.1"]
                 [compojure "1.1.5"]
                 [ring-server "0.2.8" :exclusions [ring/ring-jetty-adapter]]
                 [http-kit "2.1.10"]
                 [cheshire "5.2.0"]
                 [clj-wamp "1.0.0"]
                 [hiccup "1.0.4"]

                 ; Clojurescript
                 [hiccups "0.2.0"]

                 ; Testing
                 [clj-http "0.7.6"]
                 ; Testing that includes the filesystem (project-viz)
                 [me.raynes/fs "1.4.5"]]
  :plugins [[lein-cljsbuild "0.3.2"]
            [codox "0.6.4"]]

  :codox {:include [vdd-core.core
                    vdd-core.capture-global]}

  :hooks [leiningen.cljsbuild]

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]

  :repl-options {:init-ns user}
  :profiles {:dev {:source-paths ["dev"]
                   :plugins [[com.cemerick/austin "0.1.0"]]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  ; Clojurescript testing
                                  [com.cemerick/clojurescript.test "0.0.4"]]}}

  :cljsbuild {:builds {:main {:source-paths ["src/cljs" "test/cljs"]
                              :compiler {:output-to "resources/public/vdd/vdd-core.js"
                                         :optimizations :whitespace
                                         :pretty-print true}
                              :jar true}}
              :test-commands {"unit-test" ["phantomjs" "test/cljs/runner.js" "resources/public/vdd/vdd-core.js"]}})
