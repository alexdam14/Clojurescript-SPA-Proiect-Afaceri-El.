(ns dv.fw.client.demo
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dv.fw.client.ui :as ui]
            [dv.fw.client.extended-demo :as ed]
            [reagent.core :as r]
            [reagent-forms.core :as rf
                                :refer [bind-fields init-field value-of]]
            [json-html.core :refer [edn->hiccup]]
            cljsjs.jquery
            cljsjs.bootstrap))

(def persons
  [{:name "Popescu"
    :surname "Ion"
    :age 27
    :birthDate                                              (js/Date. 1988 5 11)
    :id 1}
   {:name "Ionescu"
    :surname "Gheorghe"
    :age 30
    :birthDate                                              (js/Date. 1994 9 24)
    :id 2}])

(def pers-atom (r/atom persons))
(def selected-atom (r/atom nil))
(def table-selection (r/atom #{}))

(defn p-update [pers-atom selected-atom]
  (let [sel-atom @selected-atom
        id-to-search (:id sel-atom)
        persons @pers-atom]
    (for [p persons]
      (if (= (:id p) id-to-search)
        (assoc p :name (:name sel-atom)
                 :surname (:surname sel-atom)
                 :age (:age sel-atom)
                 :birthDate (:birthDate sel-atom))
        p))))

(defn remove-from-list [pers-atom selected-atom]
  (let [sel-atom @selected-atom
        id-to-search (:id sel-atom)
        persons @pers-atom]
    (remove (fn [pers]
              (= id-to-search (:id pers)))
            persons)))

(defn table [pers-atom]
  [:table.table.table-hover
   [:thead
    [:tr
     [:th.name "Name"]
     [:th.surname "Surname"]
     [:th.age "Age"]
     [:th.birthDate "Birth Date"]
     [:th.id "ID"]]]
   [:tbody
    (for [p @pers-atom]
      [:tr {:on-click
            #(reset! selected-atom {:name (:name p)
                                    :surname (:surname p)
                                    :age (:age p)
                                    :birthDate (:birthDate p)
                                    :id (:id p)})}
       [:td.tdname (:name p)]
       [:td.tdsurname (:surname p)]
       [:td.tdage (:age p)]
       [:td.tdbirthDate (str (.getFullYear (:birthDate p)) "-" (.getMonth (:birthDate p)) "-" (.getDate (:birthDate p)))]
       [:td.tdid (:id p)]])]])

(defn row [label input]
  [:div.row
   [:label.label.label-primary label] input])

(def form-template
  [:div
   (row "Name"
        [:input.form-control {:field :text :id :name}])
   [:br]
   (row "Surname"
        [:input.form-control {:field :text :id :surname}])
   [:br]
   (row "Age"
        (ui/spinner :age 1 99))
   [:br]
   (row "Birth Date"
        [:input.form-control {:field :datepicker :id :birthDate :date-format "yyyy/mm/dd" :inline true
                              :in-fn #(when % {:year (.getFullYear %) :month (.getMonth %) :day (.getDate %)})
                              :out-fn #(when % (js/Date. (:year %) (:month %) (:day %)))}])
   [:br]
   (row "ID"
        [:input.form-control {:field :numeric :id :id}])
   [:br]])

(defn form [selected-atom]
  [:div.forms
   [rf/bind-fields form-template selected-atom]])

(defn buttons []
  [:div.btn-group.btn-group-justified
   [:div.btn-group {:field :single-select :id :buttons}
    [:button.btn.btn-primary {:on-click
                                   #(let [pers @pers-atom
                                          sel @selected-atom]
                                     (do (reset! pers-atom (conj pers {:name (:name sel)
                                                                       :surname (:surname sel)
                                                                       :age (:age sel)
                                                                       :birthDate (js/Date. (:year (:birthDate sel))
                                                                                            (:month (:birthDate sel))
                                                                                            (:day (:birthDate sel)))
                                                                       :id (:id sel)}))
                                         (reset! selected-atom nil)))
                              :key :add}
     "Add"]]
   [:div.btn-group {:field :single-select :id :buttons}
    [:button.btn.btn-primary {:on-click
                                   #(let [updated-pers (p-update pers-atom selected-atom)]
                                     (do (reset! pers-atom updated-pers)
                                         (reset! selected-atom nil)))

                              :key :edit} "Edit"]]
   [:div.btn-group {:field :single-select :id :buttons}
    [:button.btn.btn-primary {:on-click
                                   #(let [updated-pers (remove-from-list pers-atom selected-atom)]
                                     (do (reset! pers-atom updated-pers)
                                         (reset! selected-atom nil)))

                              :key :edit} "Remove"]]])

(defn reagent-main-view []
  [:div.reagent-main-view.col-md-12
   [table pers-atom]
   [form selected-atom]
   [buttons]])


(def lorem-ipsum "Lorem ipsum dolor sit amet, mel reque salutandi at, zril oportere evertitur no mea.
Quidam fierent reprehendunt et quo, mei et melius meliore moderatius, stet dicam ei usu. Sit no decore bonorum.
Audiam inimicus has at, est ea magna suscipiantur. Eos nulla civibus ex, sapientem disputationi pri at, mucius comprehensam vis ea.
Graece ponderum periculis at est, cum id graece indoctum partiendo. Ornatus maiestatis interpretaris quo ne, pri duis postulant salutatus ei, ut pro amet liber menandri
\n\n Vocibus electram consulatu his no, sea tempor accusam ut, mea discere definitiones cu. Et sit habemus invenire.
Urbanitas intellegat et eos, nusquam luptatum intellegebat ne duo, eum et meliore alienum forensibus. Eripuit sadipscing cu sed, fierent ponderum at his.
Noster indoctum incorrupte usu ea. Ut enim liber viderer sea.\n\n Vim sumo sapientem at, ea vix autem porro deterruisset. Ne assum choro eam. Suscipit similique cum et.
Lorem phaedrum ad usu. Illud altera vulputate qui ex, mel no vide concludaturque.
Vim harum nostrum ut, quo ne posse minim aeque.\n\n Eam et option definiebas, cu pri lorem fierent signiferumque, volumus invidunt tacimates cu vel.
o purto recteque usu. Cum ex omnes equidem detracto, et decore veritus postulant pri. Ut his aperiam inermis, duo viderer aliquid ex.
Has agam porro disputationi cu, dicunt maiorum minimum te qui. Ex alia option quaerendum nec.\n\n Docendi voluptua sea cu, qui cu quis dolorem lobortis.
Mediocrem neglegentur at mei, at pro amet molestiae hendrerit, eos fabulas utroque fabellas ad. Qui quaeque probatus rationibus ea, ut mei rebum sensibus. Cu legimus.")

(def test-atom (r/atom nil))
(def popup1 (r/atom false))
(def popup2 (r/atom false))
(def ok-cancel-state (r/atom false))

(defn ui-test-view []
  [:div
   [rf/bind-fields
    [:div
     [:div.row {:style {:margin-bottom "15px"}}
      (ui/tab-view [["Scroll Composite" (ui/scroll-composite [:p lorem-ipsum])]
                    ["List box" (ui/list-box [[1 "Andrei"] [2 "Marcel"] [3 "Maria"] [4 "Ana"]] nil)]]
                   "Scroll Composite"
                   true)]

     [:div.col-md-5 {:style {:margin-bottom "3px"}}
      (ui/button "button1" {:class "btn btn-default"} "http://orig12.deviantart.net/250e/f/2012/327/5/4/5405ca7130582d6cbda8cbe0bb0fc9a8-d5lwex1.gif"
                 #(js/alert "btntest") "btntest")
      (ui/button "button2" {:class "btn btn-default"} "http://icons.iconarchive.com/icons/custom-icon-design/pretty-office-8/128/Accept-icon.png")]
     [:div.col-md-4 {:style {:margin-bottom "3px"}}
      (ui/tool-bar nil
                   (ui/button "button3" nil "http://megaicons.net/static/img/icons_title/46/122/title/arrow-download-icon.png")
                   (ui/button "button4" nil "http://keningaufm.rtm.gov.my/keningaufm/images/001671-ultra-glossy-silver-button-icon-media-music-eighth-notes.png")
                   (ui/tool-menu "dropdown" [[:a {:key "droptext1"} "droptext1"]
                                             [:a {:key "droptext2"} "droptext2"]]))]


     [:div.col-xs-12 {:style {:margin-top "3px"
                              :margin-bottom "3px"}}
      [:div.col-xs-3 (ui/image "http://mercedesblog.ro/wp-content/uploads/2014/12/2014-Mercedes-Benz-S-Class1.jpg" {:style {:border-radius "7%"}} "Mercedes S350")]]

     [:div.col-md-6
      [:div {:style {:margin-bottom "3px"}}
       (ui/label "single-line-string" {:class "label label-default"})
       (ui/single-line-string :name {:placeholder "text"} "single-line-string")]
      [:div {:style {:margin-bottom "3px"}}
       (ui/label "multi-line-string" {:class "label label-default"})
       (ui/multi-line-string :name {:placeholder "text" :rows 4 :cols 5 :style {:margin-bottom "3px"}} "multi-line-string")]]

     [:div.col-md-6
      [:div (ui/label "age" {:class "label label-default"})
       (ui/spinner :age 0 99 {:style {:margin-bottom "3px"}} "age")]
      [:div {:style {:margin-bottom "3px"}} (ui/date-field :date nil "date-field")]
      (ui/date-time-field :datetime {:style {:margin-bottom "3px"}} "date-time-field")
      [:div (ui/label "select-box" {:class "label label-default"})
       (ui/select-box "select"
                      ["Andrei" "Marcel" "Ionel" "Maria"]
                      {:input-placeholder "Typeahead - Select-box"})]]

     [:div.row
      [:div.col-md-1 (ui/check-box "yes-checkbox" "YES" nil "check-box")]]

     [:div.row
      (ui/composite nil [:div.col-md-6 {:key "composite-div-1"} (ui/multi-line-string :name {:placeholder "composite-multi-line-string" :rows 2 :cols 5
                                                                                             :style {:margin-bottom "3px"}} "composite-multi-line-string")]
                    [:div.col-md-6 {:key "composite-div-2"} (ui/single-line-string :name {:placeholder "composite-single-line-string"} "composite-single-line-string")])]

     [:div.row
      [ui/tree-table
       [:root :additional1 :additional2]
       ["ROOT" "Additional info 1" "Additional info 2"]
       [{:data {:root "level 1-1"
                :additional1 "AD I 1-1"
                :additional2 "AD II 1-1"}
         :children [{:data {:root "level 1-2"
                            :additional1 "AD I 1-2"
                            :additional2 "AD II 1-2"}
                     :children [{:data {:root "level 1-3"
                                        :additional1 "AD I 1-3"
                                        :additional2 "AD II 1-3"}
                                 :children nil}]}]}
        {:data {:root "level 2-1"
                :additional1 "AD I 2-1"
                :additional2 "AD II 2-1"}
         :children [{:data {:root "level 2-2"
                            :additional1 "AD I 2-2"
                            :additional2 "AD II 2-2"}
                     :children nil}]}
        {:data {:root "level 3-1"
                :additional1 "AD I 3-1"
                :additional2 "AD II 3-1"}
         :children [{:data {:root "level 3-2"
                            :additional1 "AD I 3-2"
                            :additional2 "AD II 3-2"}
                     :children [{:data {:root "level 3-3"
                                        :additional1 "AD I 3-3"
                                        :additional2 "AD II 3-3"}
                                 :children nil}]}]}]]]

     [:div.col-md-12
      [ui/table [:a :c :b]
                      ["Column a" "Column c" "Column b"]
                      (r/atom [{:a 1
                                :b "gigi"
                                :c "Ion"}
                               {:a 2
                                :b "gheorghe"
                                :c "mihai"}])
                      nil
                      nil
                      table-selection]]] test-atom]

  [:div.col-md-12
   (ui/button "Message Box" nil nil
              #(reset! popup1
                       true))
   (ui/message-box popup1
                   "Title"
                   "Message")
   (ui/message-box popup2
                   "Title"
                   (let [popup2-1 (r/atom false)]
                     [:div
                      (ui/button "Message Box2" nil nil
                                 #(reset! popup2-1
                                          true))
                      (ui/message-box popup2-1
                                      "Title2"
                                      "Message2")]))
   (ui/button "Ok Cancel Dialog" nil nil
              #(reset! ok-cancel-state
                       true))

   (ui/button "Multi Message Box" nil nil
              #(reset! popup2
                       true))
   (ui/ok-cancel-dialog ok-cancel-state
                        "Title"
                        [:div {:style {:margin-bottom "3px"}}
                         (ui/label "Write something" {:class "label label-default"})
                         (ui/multi-line-string :name {:placeholder "Write something..."} "WrSmth")]
                        #(js/alert "Ok"))
   [:div.col-md-12
    [#(edn->hiccup @table-selection)]]]])

(defn ^:export main-demo []
  #_(r/render-component [reagent-main-view] (.getElementById js/document "uitests"))
  #_(r/render-component (ui-test-view)
                      (.getElementById js/document "uitests"))
  (r/render-component (ed/extended-demo-view)
                      (.getElementById js/document "uitests")))