(defproject cljow-log "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-cljsbuild "1.1.7"]]
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/tools.logging "0.4.1"]]
  :source-paths ["src/cljc"]
  :pedantic? :abort
  :profiles {:dev {:dependencies [[org.clojure/core.async "0.4.500"]]}}
  :cljsbuild {:builds [{:id "nodejs"
                        :source-paths ["src/cljc"]
                        :compiler {:target :nodejs
                                   :main ow.logging
                                   :output-dir "lib/cljs/cljow-log"
                                   :output-to "lib/cljs/cljow-log.js"
                                   :optimizations :none
                                   ;;;:source-map-timestamp true
                                   ;;;:print-input-delimiter true
                                   :pretty-print true
                                   ;;;:output-wrapper true
                                   :verbose true}}]})
