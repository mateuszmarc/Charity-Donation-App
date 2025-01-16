document.addEventListener("DOMContentLoaded", function() {
  console.log("DOM loaded")
  /**
   * Form Select
   */
  class FormSelect {
    constructor($el) {
      this.$el = $el;
      this.options = [...$el.children];
      this.init();
    }

    init() {
      this.createElements();
      this.addEvents();
      this.$el.parentElement.removeChild(this.$el);
    }

    createElements() {
      // Input for value
      this.valueInput = document.createElement("input");
      this.valueInput.type = "text";
      this.valueInput.name = this.$el.name;

      // Dropdown container
      this.dropdown = document.createElement("div");
      this.dropdown.classList.add("dropdown");

      // List container
      this.ul = document.createElement("ul");

      // All list options
      this.options.forEach((el, i) => {
        const li = document.createElement("li");
        li.dataset.value = el.value;
        li.innerText = el.innerText;

        if (i === 0) {
          // First clickable option
          this.current = document.createElement("div");
          this.current.innerText = el.innerText;
          this.dropdown.appendChild(this.current);
          this.valueInput.value = el.value;
          li.classList.add("selected");
        }

        this.ul.appendChild(li);
      });

      this.dropdown.appendChild(this.ul);
      this.dropdown.appendChild(this.valueInput);
      this.$el.parentElement.appendChild(this.dropdown);
    }

    addEvents() {
      this.dropdown.addEventListener("click", e => {
        const target = e.target;
        this.dropdown.classList.toggle("selecting");

        // Save new value only when clicked on li
        if (target.tagName === "LI") {
          this.valueInput.value = target.dataset.value;
          this.current.innerText = target.innerText;
        }
      });
    }
  }
  document.querySelectorAll(".form-group--dropdown select").forEach(el => {
    new FormSelect(el);
  });

  /**
   * Hide elements when clicked on document
   */
  document.addEventListener("click", function(e) {
    const target = e.target;
    const tagName = target.tagName;

    if (target.classList.contains("dropdown")) return false;

    if (tagName === "LI" && target.parentElement.parentElement.classList.contains("dropdown")) {
      return false;
    }

    if (tagName === "DIV" && target.parentElement.classList.contains("dropdown")) {
      return false;
    }

    document.querySelectorAll(".form-group--dropdown .dropdown").forEach(el => {
      el.classList.remove("selecting");
    });
  });

  /**
   * Switching between form steps
   */
  class FormSteps {
    constructor(form) {
      this.$form = form;
      this.$next = form.querySelectorAll(".next-step");
      this.$prev = form.querySelectorAll(".prev-step");
      this.$step = form.querySelector(".form--steps-counter span");
      this.currentStep = 1;

      this.$stepInstructions = form.querySelectorAll(".form--steps-instructions p");
      const $stepForms = form.querySelectorAll("form > div");
      this.slides = [...this.$stepInstructions, ...$stepForms];

      this.init();
    }

    /**
     * Init all methods
     */
    init() {
      this.events();
      this.updateForm();
    }

    /**
     * All events that are happening in form
     */
    events() {
      // Next step
      this.$next.forEach(btn => {
        btn.addEventListener("click", e => {
          e.preventDefault();

          this.currentStep++;
          this.updateForm();
        });
      });

      // Previous step
      this.$prev.forEach(btn => {
        btn.addEventListener("click", e => {
          e.preventDefault();
          this.currentStep--;
          this.updateForm();
        });
      });
      //
      // // Form submit
      // this.$form.querySelector("form").addEventListener("submit", e => this.submit(e));
    }

    /**
     * Update form front-end
     * Show next or previous section etc.
     */
    updateForm() {
      this.$step.innerText = this.currentStep;
      // TODO: Validation


      this.slides.forEach(slide => {

          slide.classList.remove("active");
          if (slide.dataset.step == this.currentStep) {
            slide.classList.add("active");
          }
      });

      this.$stepInstructions[0].parentElement.parentElement.hidden = this.currentStep >= 5;
      this.$step.parentElement.hidden = this.currentStep >= 5;

      // TODO: get data from inputs and show them in summary

      setTimeout(() => {
        const selectedCategories = [];
        const formSummaryElements = document.querySelector('[data-step="5"]').querySelectorAll("li");
        this.slides.forEach(slide => {
          if (slide instanceof HTMLDivElement) {
            if (slide.dataset['step'] === "1") {
              slide.querySelectorAll("input").forEach(input => {
                if (input.checked) {
                  selectedCategories.push(input.nextElementSibling.nextElementSibling.nextElementSibling.innerText);
                }
              });
            }

            if (slide.dataset['step'] === "2") {
              const bags = (slide.querySelector("input").value);
              const categoriesAndBags = formSummaryElements[0].querySelector("span").nextElementSibling;
              categoriesAndBags.innerText = bags + " worki z kategorii: " + selectedCategories.join(", ");
            }

            if (slide.dataset['step'] === "3") {
              let institution = ""
              slide.querySelectorAll("input").forEach(input => {
                if (input.checked) {
                  institution = input.nextElementSibling.nextElementSibling.querySelector(".title").innerText;
                }
              })
              const institutionTag = formSummaryElements[1].querySelector("span").nextElementSibling;
              institutionTag.innerText = "Dla organizacji " + institution;
            }

            if (slide.dataset['step'] === "4") {
              const inputTags = slide.querySelectorAll("input")
              const street = inputTags[0].value;

              const city = inputTags[1].value;

              const zipCode = inputTags[2].value;

              const phoneNumber = inputTags[3].value;

              const date = inputTags[4].value;

              const hour = inputTags[5].value;

              const comment = slide.querySelector("textarea").value;

              formSummaryElements[2].innerText = street;
              formSummaryElements[3].innerText = city;
              formSummaryElements[4].innerText = zipCode;
              formSummaryElements[5].innerText = phoneNumber;
              formSummaryElements[6].innerText = date;
              formSummaryElements[7].innerText = hour;
              formSummaryElements[8].innerText = comment;
            }
          }

        });
      }, 0.001);

    }
  }


  const form = document.querySelector(".form--steps");
  if (form !== null) {
    new FormSteps(form);
  }

  const deleteForms = document.querySelectorAll(".delete-form");
  const confirmationModal = document.getElementById("confirmationModal");
  const confirmDeleteButton = document.getElementById("confirmDelete");
  const cancelDeleteButton = document.getElementById("cancelDelete");
  let formToSubmit = null;

  deleteForms.forEach((form) => {
    form.addEventListener("submit", (event) => {
      event.preventDefault(); // Prevent immediate form submission

      // Show the modal
      confirmationModal.style.display = "block";

      // Store the form reference to submit later if confirmed
      formToSubmit = form;
    });
  });

  if (confirmDeleteButton) {
    // Handle confirmation
    confirmDeleteButton.addEventListener("click", () => {
      if (formToSubmit) {
        formToSubmit.submit(); // Submit the form if confirmed
      }
      confirmationModal.style.display = "none"; // Hide the modal
    });
  }

  if (cancelDeleteButton) {
    // Handle cancellation
    cancelDeleteButton.addEventListener("click", () => {
      confirmationModal.style.display = "none"; // Hide the modal
      formToSubmit = null; // Clear the stored form reference
    });
  }

  // Close the modal when clicking outside the content
  window.addEventListener("click", (event) => {
    if (event.target === confirmationModal) {
      confirmationModal.style.display = "none";
      formToSubmit = null;
    }
  });

});