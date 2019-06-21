(ns ow.logging.clojure
  (:refer-clojure :rename {defn defn-clj
                           fn   fn-clj
                           let  let-clj})
  #?(:cljs (:require-macros [ow.logging.core :as cm]))
  #?(:clj  (:require [ow.logging.core :as c]
                     [ow.logging.core :as cm])
     :cljs (:require [ow.logging.core :as c])))

#_(defn-clj lhs-aliases [args]
  (letfn [(map-name [m]
            (str (or (reduce (fn-clj [name [k v]]
                                     (if (= k :as)
                                       v
                                       name))
                             nil m)
                     "destructed-map")
                 "-"))

          (vec-name [v]
            (str (or (some-> (reduce (fn-clj [[capture? name] elem]
                                             (cond
                                               capture?     [false elem]
                                               (= elem :as) [true  name]
                                               true         [false name]))
                                     [false nil] v)
                             second)
                     "destructed-vector")
                 "-"))

          (alias-arg [arg]
            (cond
              (map? arg)    (gensym (map-name arg))
              (vector? arg) (gensym (vec-name arg))
              (symbol? arg) (if (not= arg '&)
                              (gensym (str arg "-"))
                              arg)
              true          (throw (ex-info "don't know how to create alias for arg" {:arg arg}))))]

    (map alias-arg args)))

#_(defn-clj remove-& [lhs-aliases]
  (remove #(= % '&) lhs-aliases))



#_(defmacro fn
  "Same as clojure.core/fn, but also adds a logging checkpoint upon invocation of the fn."
  [name [& args] & body]
  (let-clj [lhs (lhs-aliases args)
            rhs (remove-& lhs)]  ;; simplify e.g. destructuring
    `(fn-clj ~name [~@lhs]
             (cm/with-checkpoint* ~name [~@rhs]
               (let-clj [[~@(remove-& args)] [~@rhs]]
                 ~@body)))))

#_(defmacro defn
  "Same as clojure.core/defn, but also adds a logging checkpoint upon invocation of the defn."
  [name [& args] & body]
  (let-clj [lhs (lhs-aliases args)
            rhs (remove-& lhs)]  ;; simplify e.g. destructuring
    `(defn-clj ~name [~@lhs]
       (cm/with-checkpoint* ~name [~@rhs]
         (let-clj [[~@(remove-& args)] [~@rhs]]
           ~@body)))))

#_(defmacro let
  "Like clojure.core/let, but also sets the current logging info to
  potential logging info maps that might be attached to the given values."
  [[& bindings] & body]
  (let-clj [bindings (partition 2 bindings)
            rhs      (->> bindings (map first) lhs-aliases remove-&)]
    `(let-clj [~@(mapcat (fn-clj [[sym value] alias]
                                 `[~alias ~value])
                         bindings rhs)
               ~@(mapcat (fn-clj [[sym value] alias]
                                 `[~sym ~alias])
                         bindings rhs)]
       (cm/with-logging-info (apply c/merge-historical-logging-infos (list ~@rhs (c/current-logging-info)))
         ~@body))))
