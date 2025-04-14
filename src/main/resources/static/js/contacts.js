console.log("contacts.js loaded");
const viewContactModal = document.getElementById("view_contact_modal");
const baseURL = "http://localhost:8082";

// options with default values
const options = {
  placement: "bottom-right",
  backdrop: "dynamic",
  backdropClasses: "bg-gray-900/50 dark:bg-gray-900/80 fixed inset-0 z-40",
  closable: true,
  onHide: () => {
    console.log("modal is hidden");
  },
  onShow: () => {
    // setTimeout(() => {
    //   contactModal.classList.add("scale-100");
    // }, 50);
    console.log("modal is shown");
  },
  onToggle: () => {
    console.log("modal has been toggled");
  },
};

// instance options object
const instanceOptions = {
  id: "view_contact_modal",
  override: true,
};

const contactModal = new Modal(viewContactModal, options, instanceOptions);

function openContactModal() {
  contactModal.show();
}

function closeContactModal() {
  contactModal.hide();
}

async function loadContactdata(id) {
  //function call to load data
  console.log(id);
  try {
    const data = await (await fetch(`${baseURL}/api/contact/${id}`)).json();
    document.querySelector("#contact_name").innerHTML = data.name;
    document.querySelector("#contact_email").innerHTML = data.email;
    document.querySelector("#contact_image").src = data.imageUrl;
    let address = data.address.street + ", " + data.address.city + ", " + data.address.state + ", " + data.address.country + ", " + data.address.zipCode;
    document.querySelector("#contact_address").innerHTML = address;
    document.querySelector("#contact_phone").innerHTML = data.phoneNumber;
    document.querySelector("#contact_about").innerHTML = data.about;
    const contactFavorite = document.querySelector("#contact_favorite");
    if (data.favorite) {
      contactFavorite.innerHTML =
        "<i class='fas fa-star text-yellow-400'></i><i class='fas fa-star text-yellow-400'></i><i class='fas fa-star text-yellow-400'></i><i class='fas fa-star text-yellow-400'></i><i class='fas fa-star text-yellow-400'></i>";
    } else {
      contactFavorite.innerHTML = "Not Favorite Contact";
    }

    document.querySelector("#contact_website").href = data.website;
    document.querySelector("#contact_website").innerHTML = data.website;
    document.querySelector("#contact_linkedIn").href = data.linkedin;
    document.querySelector("#contact_linkedIn").innerHTML = data.linkedin;
    openContactModal();
  } catch (error) {
    console.log("Error: ", error);
  }
}

async function deleteContact(id) {
    console.log("delete contact called", id);
    Swal.fire({
      title: "Do you want to delete the contact?",
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Delete",
      confirmButtonColor: "#336eff",
      cancelButtonColor: "#e71518"

    }).then((result) => {
      if (result.isConfirmed) {
        const url = `${baseURL}/user/contacts/delete/` + id;
        window.location.replace(url);
      }
    });
  }