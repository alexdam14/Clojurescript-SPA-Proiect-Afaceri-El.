(defproject reactom "1.0.0"
  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-deps-tree "0.1.2"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89"]
                 [cljsjs/bootstrap "3.3.6-1" :exclude [cljsjs/jquery]]
                 [cljsjs/jquery "2.2.2-0"]
                 [json-html "0.4.0"] ; edn->hiccup
                 [org.clojure/core.async "0.2.385"]
                 [reagent "0.6.0-SNAPSHOT"]
                 [reagent-forms "0.5.24" :exclude [reagent]]]
  :source-paths ["src/cljc"]
  :test-paths ["test/cljc"]
  :cljsbuild {:builds [{:source-paths ["src/cljs"
                                       "src/cljc"]
                        :test-paths   ["test/cljs"
                                       "test/cljc"]
                        :compiler     {:source-map-path "maps/"
                                       :output-dir      "target/maps/"
                                       :source-map      "target/map.js"
                                       :output-to       "target/main.js"
                                       :foreign-libs  [{:file "resources/jquery.treegrid.js",
                                                        :provides ["jquery.treetable"],
                                                        :requires ["cljsjs.jquery"]}]}}]})
