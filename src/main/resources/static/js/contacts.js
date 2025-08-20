console.log("contacts.js loaded");
const viewContactModal = document.getElementById("view_contact_modal");
const baseURL = "http://localhost:8080";
// const baseURL = "http://conatctmanager.us-east-2.elasticbeanstalk.com";

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
      cancelButtonText: "Cancel",
      background: document.documentElement.classList.contains('dark') ? '#1a202c' : '#fff',
      color: document.documentElement.classList.contains('dark') ? '#fff' : '#222',
      customClass: {
        confirmButton: 'swal2-confirm bg-blue-700 text-white px-4 py-2 rounded hover:bg-blue-800 mx-2',
        cancelButton: 'swal2-cancel bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 mx-2',
        popup: document.documentElement.classList.contains('dark') ? 'dark-mode-swal' : ''
      },
      buttonsStyling: false
    }).then((result) => {
      if (result.isConfirmed) {
        const url = `${baseURL}/user/contacts/delete/` + id;
        window.location.replace(url);
      }
    });
  }
  // Download QR code image by id
function downloadQR(id, qrUrl) {
  let url = qrUrl && qrUrl.trim() ? qrUrl.trim() : '';
  if (!url) {
    const img = document.getElementById('qr-img-' + id);
    if (img) url = img.src;
  }
  if (!url) return alert('QR code not found.');
  const a = document.createElement('a');
  a.href = url;
  a.download = 'contact_qr_' + id + '.png';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}

// Share QR code image by id
function shareQR(id, qrUrl) {
  let url = qrUrl && qrUrl.trim() ? qrUrl.trim() : '';
  if (!url) {
    const img = document.getElementById('qr-img-' + id);
    if (img) url = img.src;
  }
  if (!url) return alert('QR code not found.');
  if (navigator.share) {
    navigator.share({
      title: 'Contact QR',
      url: url
    });
  } else {
    alert('Share not supported on this browser.');
  }
}

// Show QR modal using SweetAlert2
function showQRModal(id, qrUrl) {
  Swal.fire({
    title: 'Contact QR',
    html: `<div style='display:flex;justify-content:center;align-items:center;'><img src='${qrUrl}' alt='QR Code' style='width:180px;height:180px;margin-bottom:10px;'/></div>`,
    showCloseButton: true,
    showConfirmButton: false,
    background: document.documentElement.classList.contains('dark') ? '#1a202c' : '#fff',
    color: document.documentElement.classList.contains('dark') ? '#fff' : '#222',
    customClass: {
      popup: document.documentElement.classList.contains('dark') ? 'dark-mode-swal' : ''
    }
  });
}

// Export selected contacts to Excel (contacts page)
function exportData(isFavorite) {
  const table = document.querySelector('table');
  const checkboxes = table.querySelectorAll("tbody input[type='checkbox']:checked");
  const rows = Array.from(checkboxes).map(checkbox => checkbox.closest('tr'));
  if (rows.length === 0) {
    alert('No rows selected for export.');
    return;
  }
  const customTable = document.createElement('table');
  const thead = document.createElement('thead');
  const headerRow = document.createElement('tr');
  const headers = isFavorite
    ? ["Name", "Email", "Phone", "Relationship", "Address", "Links", "About", "Image"]
    : ["Name", "Email", "Phone", "Relationship", "Address", "Links", "About", "Image", "Favorite"];
  headers.forEach(headerText => {
    const th = document.createElement('th');
    th.textContent = headerText;
    headerRow.appendChild(th);
  });
  thead.appendChild(headerRow);
  customTable.appendChild(thead);
  const tbody = document.createElement('tbody');
  rows.forEach(row => {
    const name = row.querySelector("td:nth-child(2) div.text-base")?.textContent.trim() || "N/A";
    const email = row.querySelector("td:nth-child(2) div.font-normal")?.textContent.trim() || "N/A";
    const phone = row.querySelector("td:nth-child(3) span").textContent.trim();
    const relationship = row.querySelector("td:nth-child(6)")?.textContent.trim() || "N/A";
    const address = row.querySelector("td:nth-child(8) span")?.textContent.trim() || "N/A";
    const about = row.querySelector("td:nth-child(9) span")?.textContent.trim() || "N/A";
    const links = Array.from(row.querySelectorAll("td:nth-child(4) a")).map(link => link.href).join(", ") || "N/A";
    const imageUrl = row.querySelector("td:nth-child(2) img").src;
    const image = imageUrl.includes("cloudinary") ? imageUrl : "N/A";
    let cells = [name, email, phone, relationship, address, links, about, image];
    if (!isFavorite) {
      const favorite = row.querySelector("td:nth-child(5) i.fa-heart") ? "Yes" : "No";
      cells.push(favorite);
    }
    const customRow = document.createElement('tr');
    cells.forEach(cellText => {
      const td = document.createElement('td');
      td.textContent = cellText;
      customRow.appendChild(td);
    });
    tbody.appendChild(customRow);
  });
  customTable.appendChild(tbody);
  TableToExcel.convert(customTable, {
    name: isFavorite ? "Selected_Favorite_Contacts.xlsx" : "Selected_Contacts.xlsx",
    sheet: {
      name: isFavorite ? "Favorite Contacts" : "Contacts"
    }
  });
}

// Select all checkboxes
function selectall() {
  const checkboxes = document.querySelectorAll("input[type='checkbox']");
  checkboxes.forEach(checkbox => {
    checkbox.checked = !checkbox.checked;
  });
}