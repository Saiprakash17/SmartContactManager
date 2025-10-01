console.log("contacts.js loaded");
const viewContactModal = document.getElementById("view_contact_modal");
const baseURL = window.location.protocol + '//' + window.location.host;

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
  if (!id) {
    console.error("Contact ID is undefined");
    alert("Error: Contact ID is missing");
    return;
  }

  try {
    // Show loading state
    const loadingModal = Swal.fire({
      title: 'Loading...',
      text: 'Fetching contact details',
      allowOutsideClick: false,
      showConfirmButton: false,
      willOpen: () => {
        Swal.showLoading();
      }
    });

    const response = await fetch(`${baseURL}/api/contact/${id}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    if (!result || !result.data) {
      throw new Error("No data received from server");
    }

    const data = result.data;

    // Close loading modal
    loadingModal.close();

    // Update modal content
    document.querySelector("#contact_name").textContent = data.name || "N/A";
    document.querySelector("#contact_email").textContent = data.email || "N/A";
    document.querySelector("#contact_phone").textContent = data.phoneNumber || "N/A";
    
    const contactImage = document.querySelector("#contact_image");
    if (contactImage) {
      contactImage.src = data.imageUrl || "https://upload.wikimedia.org/wikipedia/commons/a/ac/Default_pfp.jpg";
      contactImage.onerror = function() {
        this.src = "https://upload.wikimedia.org/wikipedia/commons/a/ac/Default_pfp.jpg";
      };
    }
    
    let address = data.address ? 
      `${data.address.street || ''}, ${data.address.city || ''}, ${data.address.state || ''}, ${data.address.country || ''}, ${data.address.zipCode || ''}`.replace(/^[, ]+|[, ]+$/g, '') :
      "No address available";
    document.querySelector("#contact_address").textContent = address;
    
    document.querySelector("#contact_about").textContent = data.about || "N/A";
    
    const contactFavorite = document.querySelector("#contact_favorite");
    if (contactFavorite) {
      if (data.favorite) {
        contactFavorite.innerHTML = "<i class='fas fa-star text-yellow-400'></i>".repeat(5);
      } else {
        contactFavorite.textContent = "Not Favorite Contact";
      }
    }

    const websiteElement = document.querySelector("#contact_website");
    const linkedInElement = document.querySelector("#contact_linkedIn");
    
    if (websiteElement) {
      websiteElement.href = data.website || "#";
      websiteElement.textContent = data.website || "N/A";
    }
    
    if (linkedInElement) {
      linkedInElement.href = data.linkedin || "#";
      linkedInElement.textContent = data.linkedin || "N/A";
    }

    const relationshipElement = document.querySelector("#contact_relationship");
    if (relationshipElement && data.relationship) {
      relationshipElement.textContent = data.relationship.label || "N/A";
    }
    
    openContactModal();
  } catch (error) {
    console.error("Error loading contact data:", error);
    Swal.fire({
      title: 'Error',
      text: 'Failed to load contact details. Please try again.',
      icon: 'error',
      confirmButtonText: 'OK',
      background: document.documentElement.classList.contains('dark') ? '#1a202c' : '#fff',
      color: document.documentElement.classList.contains('dark') ? '#fff' : '#222',
      customClass: {
        confirmButton: 'swal2-confirm bg-blue-700 text-white px-4 py-2 rounded hover:bg-blue-800',
        popup: document.documentElement.classList.contains('dark') ? 'dark-mode-swal' : ''
      },
      buttonsStyling: false
    });
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
async function downloadQR(id, qrUrl) {
  try {
    let url = qrUrl && qrUrl.trim() ? qrUrl.trim() : '';
    if (!url) {
      const img = document.getElementById('qr-img-' + id);
      if (img) url = img.src;
    }
    if (!url) {
      throw new Error('QR code URL not found');
    }

    // Fetch the QR code image
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error('Failed to fetch QR code');
    }
    const blob = await response.blob();

    // Create download link
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'contact_qr_' + id + '.png';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(a.href);
  } catch (error) {
    console.error('Error downloading QR code:', error);
    Swal.fire({
      title: 'Error',
      text: error.message || 'Failed to download QR code',
      icon: 'error',
      confirmButtonText: 'OK'
    });
  }
}

  // Share QR code image by id
async function shareQR(id, qrUrl) {
  try {
    let url = qrUrl && qrUrl.trim() ? qrUrl.trim() : '';
    if (!url) {
      const img = document.getElementById('qr-img-' + id);
      if (img) url = img.src;
    }
    if (!url) {
      throw new Error('QR code URL not found');
    }

    // Fetch the QR code image
    const response = await fetch(url);
    const blob = await response.blob();
    const file = new File([blob], 'contact_qr_' + id + '.png', { type: 'image/png' });

    if (navigator.share && navigator.canShare({ files: [file] })) {
      await navigator.share({
        title: 'Contact QR Code',
        text: 'Here is the QR code for the contact',
        files: [file]
      });
    } else if (navigator.share) {
      await navigator.share({
        title: 'Contact QR Code',
        text: 'Here is the QR code for the contact',
        url: url
      });
    } else {
      throw new Error('Share not supported on this browser');
    }
  } catch (error) {
    console.error('Error sharing QR code:', error);
    Swal.fire({
      title: 'Error',
      text: error.message || 'Failed to share QR code',
      icon: 'error',
      confirmButtonText: 'OK'
    });
  }
}// Show QR modal using SweetAlert2
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

  const headers = isFavorite
    ? ["Name", "Email", "Phone", "Relationship", "Address", "Links", "About", "Image"]
    : ["Name", "Email", "Phone", "Relationship", "Address", "Links", "About", "Image", "Favorite"];

  const data = [headers];

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
    data.push(cells);
  });

  const ws = XLSX.utils.aoa_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, isFavorite ? "Favorite Contacts" : "Contacts");
  XLSX.writeFile(wb, isFavorite ? "Selected_Favorite_Contacts.xlsx" : "Selected_Contacts.xlsx");
}

// Select all checkboxes
function selectall() {
  const checkboxes = document.querySelectorAll("input[type='checkbox']");
  checkboxes.forEach(checkbox => {
    checkbox.checked = !checkbox.checked;
  });
}