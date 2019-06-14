(defproject cljow-log "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-cljsbuild "1.1.7" :exclusions [fs]]]
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.516" :scope "provided"]
                 [org.clojure/tools.logging "0.4.1"]]
  :source-paths ["src/cljc"]
  :pedantic? :abort
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[cider/piggieback "0.4.1"]
                                  #_[com.bhauman/figwheel-main "0.2.0"]
                                  #_[figwheel-sidecar "0.5.8"]
                                  [org.clojure/core.async "0.4.500"]

                                  ;;; to resolve conflicts:
                                  [com.google.errorprone/error_prone_annotations "2.1.3"]
                                  [com.google.code.findbugs/jsr305 "3.0.2"]
                                  #_[joda-time "2.9.9"]
                                  #_[commons-codec "1.11"]
                                  #_[clj-time "0.14.3"]
                                  #_[args4j "2.33"]
                                  #_[commons-fileupload "1.3.3"]
                                  #_[ring/ring-codec "1.1.1"]
                                  #_[commons-io "2.6"]
                                  #_[ring/ring-core "1.7.0"]
                                  #_[ring-cors "0.1.12"]]
                   :repl-options {:init-ns user
                                  :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}}
  :cljsbuild {:builds [{:id "nodejs"
                        :source-paths ["src/cljc"]
                        ;;;:figwheel true
                        :compiler {:target :nodejs
                                   :main ow.logging
                                   :output-dir "lib/cljs/cljow-log"
                                   :output-to "lib/cljs/cljow-log.js"
                                   :optimizations :none
                                   ;;;:source-map-timestamp true
                                   ;;;:print-input-delimiter true
                                   :pretty-print true
                                   ;;;:output-wrapper true
                                   :verbose true}}]}
  ;;;:figwheel {}
  )
