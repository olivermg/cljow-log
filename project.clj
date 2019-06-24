(defproject cljow-log "0.1.2-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-cljsbuild "1.1.7" :exclusions [fs]]
            #_[lein-figwheel "0.5.18"]  ;; starting figwheel on cmdline (we don't use it as we're using cider)
            ]
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]
                 #_[org.clojure/tools.logging "0.4.1"]

                 ;;; to resolve conflicts (due to :pedantic? :abort):
                 [com.google.errorprone/error_prone_annotations "2.1.3"]
                 [com.google.code.findbugs/jsr305 "3.0.2"]]
  :source-paths ["src/cljc"]
  :clean-targets [:target-path :compile-path "lib/cljs"]
  :pedantic? :abort

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[cider/piggieback "0.4.1"]                   ;; for embedding cljs repl into clj nrepl
                                  [com.bhauman/figwheel-main "0.2.0"]          ;; hot reloading cljs compiled code (base functionality)
                                  #_[com.bhauman/rebel-readline-cljs "0.1.4"]    ;; nice readline functionality if on console (we don't need it in cider)
                                  [figwheel-sidecar "0.5.18"]                  ;; hot reloading via nrepl (+ understanding cljsbuild configs?)
                                  [org.clojure/core.async "0.4.500"]

                                  ;;; to resolve conflicts (due to :pedantic? :abort):
                                  [joda-time "2.9.9"]
                                  [commons-codec "1.11"]
                                  [clj-time "0.14.3"]
                                  [args4j "2.33"]
                                  [commons-fileupload "1.3.3"]
                                  [ring/ring-codec "1.1.1"]
                                  [commons-io "2.6"]
                                  [ring/ring-core "1.7.0"]
                                  [ring-cors "0.1.12"]
                                  [clj-stacktrace "0.2.8"]]
                   :repl-options {:init-ns user
                                  :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}}

  :cljsbuild {:builds [{:id "nodejs-prod"
                        :source-paths ["src/cljc" "src/cljs"]
                        :compiler {:target :nodejs
                                   :main ow.logging
                                   :output-dir "lib/cljs/prod/cljow-log"
                                   :output-to "lib/cljs/cljow-log.js"
                                   :optimizations :none
                                   :source-map true
                                   ;;;:source-map-timestamp true
                                   ;;;:print-input-delimiter true
                                   :pretty-print true
                                   ;;;:output-wrapper true
                                   :verbose true}}

                       {:id "nodejs-dev"
                        :source-paths ["src/cljc" "src/cljs"]
                        :figwheel true  ;; inject figwheel socket handling code into resulting js
                        :compiler {:target :nodejs
                                   :main ow.logging
                                   :output-dir "lib/cljs/dev/cljow-log"
                                   :output-to "lib/cljs/cljow-log.js"
                                   :optimizations :none
                                   :source-map true
                                   ;;;:source-map-timestamp true
                                   ;;;:print-input-delimiter true
                                   :pretty-print true
                                   ;;;:output-wrapper true
                                   :verbose true}}]}

  :figwheel {:server-logfile "log/figwheel.log"}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy" "clojars"]
                  #_["clean"]
                  #_["uberjar"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  #_["vcs" "push"]])
