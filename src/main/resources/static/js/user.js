console.log("user.js loaded");

document
  .querySelector("#image_file_input")
  .addEventListener("change", function (event) {
    let file = event.target.files[0];
    let reader = new FileReader();
    reader.onload = function () {
      document
        .querySelector("#upload_image_preview")
        .setAttribute("src", reader.result);
    };
    reader.readAsDataURL(file);
  });

document
    .querySelector("#reset_button")
    .addEventListener("click", function () {
        document.querySelector("#upload_image_preview").setAttribute("src", "");
        document.querySelector("#image_file_input").value = "";
    });