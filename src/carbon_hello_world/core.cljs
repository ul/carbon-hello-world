(ns carbon-hello-world.core
  (:require [carbon.vdom :as vdom]
            [carbon.rx :as rx]))

(enable-console-print!)

(println "This text is printed from src/carbon-hello-world/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

;; rx/cell is like an atom, but it notifies formulas when changed
;; use @app-state to get current value
;; (reset! app-state ...) to replace value
;; (swap! app-state ...) to update it
(defonce app-state
  (rx/cell
   {:numbers [0 0 0]
    :message "Edit me"}))

(.addEventListener
 js/window "mousemove"
 (fn [e]
   (swap! app-state assoc :mouse [(.-clientX e) (.-clientY e)])))

;; rx/rx is a formula, which is recalculated every time when other formulas and cells
;; dereferenced inside it
(def numbers-sum-first-variant (rx/rx (reduce + (get @app-state :numbers))))

;; rx/cursor is a special kind of formula which can read and write at some path in cell or another cursor
(def message (rx/cursor app-state [:message]))

(def numbers (rx/cursor app-state [:numbers]))

;; then we can rewrite our sum formula
(def numbers-sum (rx/rx (reduce + @numbers)))
;; and count our numbers
(def numbers-count (rx/rx (count @numbers)))

;; components in carbon is just functions which return nested vector representing DOM
;; and can contain other components as elements
;; if you dereference cell or formula inside your component, the latter will be updated
;; when the former changes its value

(defn message-editor []
  [:label
   [:p @message]
   [:input
    {:value @message
     :on-input #(reset! message (.. % -target -value))}
    @message]])

(defn counters []
  [:div
   (for [i (range @numbers-count)]
     ;; for a collection of similar items under the same parent we need to put a unique :key in metadata
     ^{:key i}
     [:button
      {:on-click #(swap! numbers update i inc)}
      "Number " i " = " (get @numbers i)])
   [:div "Sum = " @numbers-sum]])

(defn mouse-coords []
  (let [[x y] (get @app-state :mouse)]
    [:h3 "Mouse Coords: (" x "," y ")"]))

(defn app []
  [:div
   [:h3 "Hello, world!"]
   [counters]
   [:hr]
   [message-editor]
   [:hr]
   [mouse-coords]])

;; to actually show page we need to mount it to specific element
(vdom/mount [app] (js/document.getElementById "app"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
