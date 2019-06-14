(ns user
  (:require [cider.piggieback :as p]
            [cljs.repl.node :as rn]
            #_[figwheel-sidecar.repl-api :as f]))

#_(defn start-figwheel []
  (f/start-figwheel! "nodejs"))

#_(defn stop-figwheel []
  (f/stop-figwheel!))

(defn node-repl []
  (p/cljs-repl (rn/repl-env))
  #_(f/cljs-repl "nodejs"))
